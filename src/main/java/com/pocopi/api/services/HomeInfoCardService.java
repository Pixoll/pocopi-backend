package com.pocopi.api.services;

import com.pocopi.api.dto.config.InformationCardUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.HomeInfoCardRepository;
import com.pocopi.api.repositories.ImageRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HomeInfoCardService {
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public HomeInfoCardService(
        HomeInfoCardRepository homeInfoCardRepository, ImageService imageService,
        ImageRepository imageRepository
    ) {
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    public Map<String, String> processCardInformation(
        List<InformationCardUpdate> updateInformationCards,
        Map<Integer, File> updateImages
    ) {
        final Map<String, String> results = new HashMap<>();
        final List<HomeInfoCardModel> allExistingCards = homeInfoCardRepository.findAll();
        final Map<Integer, Boolean> processedCards = new HashMap<>();

        for (final HomeInfoCardModel card : allExistingCards) {
            processedCards.put(card.getId(), false);
        }

        int imageIndex = 0;
        int order = 0;

        for (final InformationCardUpdate patchCard : updateInformationCards) {
            final File cardImage = updateImages.get(imageIndex);
            imageIndex++;

            if (patchCard.id().isPresent()) {
                final Integer cardId = patchCard.id().get();
                final HomeInfoCardModel existingCard = homeInfoCardRepository
                    .findById(cardId)
                    .orElse(null);

                if (existingCard == null) {
                    results.put("card_" + cardId, "Card not found");
                    order++;
                    continue;
                }

                final boolean infoChanged = checkChangeByInfoCard(existingCard, patchCard);
                final boolean orderChanged = existingCard.getOrder() != order;

                final boolean hasImageChange = cardImage != null;
                final boolean deleteImage = hasImageChange && cardImage.length() == 0;
                final boolean replaceImage = hasImageChange && cardImage.length() > 0;

                if (infoChanged || orderChanged || deleteImage || replaceImage) {
                    existingCard.setTitle(patchCard.title());
                    existingCard.setDescription(patchCard.description());
                    existingCard.setColor(patchCard.color());
                    existingCard.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromCard(existingCard);
                        results.put("card_" + cardId + "_image", "Image deleted");
                    } else if (replaceImage) {
                        try {
                            updateOrCreateCardImage(existingCard, cardImage);
                            results.put("card_" + cardId + "_image", "Image updated");
                        } catch (IOException e) {
                            results.put(
                                "card_" + cardId + "_image_error",
                                "Failed to process image: " + e.getMessage()
                            );
                        }
                    }

                    homeInfoCardRepository.save(existingCard);
                    results.put("card_" + cardId, "Updated successfully");
                } else {
                    results.put("card_" + cardId, "No changes");
                }

                processedCards.put(cardId, true);
            } else {
                final HomeInfoCardModel newCard = new HomeInfoCardModel();
                newCard.setTitle(patchCard.title());
                newCard.setDescription(patchCard.description());
                newCard.setColor(patchCard.color());
                newCard.setOrder((byte) order);

                if (cardImage != null && cardImage.length() > 0) {
                    try {
                        updateOrCreateCardImage(newCard, cardImage);
                        results.put("card_new_" + order + "_image", "Image added");
                    } catch (IOException e) {
                        results.put("card_new_" + order + "_image_error", "Failed to process image: " + e.getMessage());
                    }
                }

                final HomeInfoCardModel savedNewCard = homeInfoCardRepository.save(newCard);
                results.put("card_new_" + order, "Created with ID: " + savedNewCard.getId());
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedCards.entrySet()) {
            if (!entry.getValue()) {
                final HomeInfoCardModel cardToDelete = homeInfoCardRepository.findById(entry.getKey()).orElse(null);
                if (cardToDelete != null) {
                    deleteCardWithImage(cardToDelete);
                    results.put("card_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void updateOrCreateCardImage(HomeInfoCardModel card, File imageFile) throws IOException {
        final byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        final ImageModel currentImage = card.getIcon();

        if (currentImage != null) {
            imageService.saveImageBytes(imageBytes, currentImage.getPath());
        } else {
            final String url = imageService.createAndSaveImageBytes(
                imageBytes,
                "/home/cards",
                imageFile.getName(),
                "Home info card: " + card.getTitle()
            );
            final String path = url.substring(url.indexOf("/images/") + 1);
            final ImageModel newImage = imageRepository.findByPath(path)
                .orElseThrow(() -> HttpException.notFound("Image with path " + path + " not found"));
            card.setIcon(newImage);
        }
    }

    private void deleteImageFromCard(HomeInfoCardModel card) {
        final ImageModel oldImage = card.getIcon();
        card.setIcon(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteCardWithImage(HomeInfoCardModel card) {
        final ImageModel imageToDelete = card.getIcon();
        if (imageToDelete != null) {
            imageService.deleteImage(imageToDelete.getPath());
        }
        homeInfoCardRepository.deleteById(card.getId());
    }

    private boolean checkChangeByInfoCard(HomeInfoCardModel savedCard, InformationCardUpdate updated) {
        return savedCard.getColor() != updated.color()
               || !Objects.equals(savedCard.getTitle(), updated.title())
               || !Objects.equals(savedCard.getDescription(), updated.description());
    }
}
