package com.pocopi.api.services;

import com.pocopi.api.dto.config.Image;
import com.pocopi.api.dto.config.InformationCard;
import com.pocopi.api.dto.config.InformationCardUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.HomeInfoCardRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class HomeInfoCardService {
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final ImageService imageService;

    public HomeInfoCardService(HomeInfoCardRepository homeInfoCardRepository, ImageService imageService) {
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.imageService = imageService;
    }

    public List<InformationCard> getCardsByConfigVersion(int configVersion) {
        return homeInfoCardRepository
            .findAllByConfigVersion(configVersion)
            .stream()
            .map(card -> {
                final Image iconByInfoCard = card.getIcon() != null
                    ? imageService.getImageById(card.getIcon().getId())
                    : null;

                return new InformationCard(
                    card.getId(),
                    card.getTitle(),
                    card.getDescription(),
                    card.getColor(),
                    iconByInfoCard
                );
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public boolean updateCards(
        ConfigModel config,
        List<InformationCardUpdate> informationCardsUpdates,
        List<MultipartFile> imageFiles
    ) {
        final List<HomeInfoCardModel> storedCards = homeInfoCardRepository.findAllByConfigVersion(config.getVersion());

        if (informationCardsUpdates == null || informationCardsUpdates.isEmpty()) {
            if (storedCards.isEmpty()) {
                return false;
            }

            final ArrayList<ImageModel> icons = new ArrayList<>();

            for (final HomeInfoCardModel card : storedCards) {
                if (card.getIcon() != null) {
                    icons.add(card.getIcon());
                }
            }

            homeInfoCardRepository.deleteAll(storedCards);

            for (final ImageModel icon : icons) {
                imageService.deleteImageIfUnused(icon);
            }

            return true;
        }

        final AtomicBoolean modified = new AtomicBoolean(false);
        final Map<Integer, HomeInfoCardModel> storedCardsMap = new HashMap<>();
        final Map<Integer, Boolean> processedCards = new HashMap<>();

        for (final HomeInfoCardModel card : storedCards) {
            storedCardsMap.put(card.getId(), card);
            processedCards.put(card.getId(), false);
        }

        int imageIndex = 0;
        byte order = 0;

        for (final InformationCardUpdate cardUpdate : informationCardsUpdates) {
            final MultipartFile cardImageFile = imageFiles.get(imageIndex++);

            final boolean isNew = cardUpdate.id() == null
                || !storedCardsMap.containsKey(cardUpdate.id());

            if (isNew) {
                final ImageModel icon = cardImageFile != null && !cardImageFile.isEmpty()
                    ? imageService.saveImageFile(ImageCategory.INFO_CARD, cardImageFile, "Information card icon")
                    : null;

                final HomeInfoCardModel newCard = HomeInfoCardModel.builder()
                    .config(config)
                    .order(order++)
                    .title(cardUpdate.title())
                    .description(cardUpdate.description())
                    .icon(icon)
                    .color(cardUpdate.color())
                    .build();

                homeInfoCardRepository.save(newCard);
                modified.set(true);
                continue;
            }

            final int cardId = cardUpdate.id();
            final HomeInfoCardModel storedCard = storedCardsMap.get(cardId);

            processedCards.put(cardId, true);

            final boolean updated = !Objects.equals(storedCard.getTitle(), cardUpdate.title())
                || !Objects.equals(storedCard.getDescription(), cardUpdate.description())
                || !Objects.equals(storedCard.getColor(), cardUpdate.color())
                || storedCard.getOrder() != order
                || cardImageFile != null;

            if (!updated) {
                order++;
                continue;
            }

            final ImageModel storedIcon = storedCard.getIcon();

            storedCard.setTitle(cardUpdate.title());
            storedCard.setDescription(cardUpdate.description());
            storedCard.setColor(cardUpdate.color());
            storedCard.setOrder(order++);

            if (cardImageFile != null) {
                if (storedIcon == null) {
                    final ImageModel newIcon = imageService.saveImageFile(
                        ImageCategory.INFO_CARD,
                        cardImageFile,
                        "Information card icon"
                    );
                    storedCard.setIcon(newIcon);
                } else if (!cardImageFile.isEmpty()) {
                    imageService.updateImageFile(storedCard.getIcon(), cardImageFile);
                } else {
                    storedCard.setIcon(null);
                }
            }

            homeInfoCardRepository.save(storedCard);

            if (storedIcon != null && storedCard.getIcon() == null) {
                imageService.deleteImageIfUnused(storedIcon);
            }

            modified.set(true);
        }

        processedCards.forEach((cardId, processed) -> {
            if (processed) {
                return;
            }

            final HomeInfoCardModel card = storedCardsMap.get(cardId);
            final ImageModel icon = card.getIcon();

            homeInfoCardRepository.delete(card);

            if (icon != null) {
                imageService.deleteImageIfUnused(icon);
            }

            modified.set(true);
        });

        return modified.get();
    }
}
