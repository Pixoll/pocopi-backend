package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.HomeInfoCard.PatchInformationCard;
import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.repositories.HomeInfoCardRepository;
import com.pocopi.api.services.interfaces.HomeInfoCardService;
import com.pocopi.api.services.interfaces.ImageService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class HomeInfoCardServiceImp implements HomeInfoCardService {
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final ImageService imageService;

    public HomeInfoCardServiceImp(HomeInfoCardRepository homeInfoCardRepository,  ImageService imageService) {
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.imageService = imageService;
    }

    @Override
    public Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, Map<Integer, File> updateImages) {
        Map<String, String> results = new HashMap<>();
        List<HomeInfoCardModel> allExistingCards = homeInfoCardRepository.findAll();
        Map<Integer, Boolean> processedCards = new HashMap<>();
        String category = "/home/cards";

        for (HomeInfoCardModel card : allExistingCards) {
            processedCards.put(card.getId(), false);
        }

        int imageIndex = 0;
        int order = 0;

        for (PatchInformationCard patchCard : updateInformationCards) {
            File cardImage = updateImages.get(imageIndex);
            imageIndex++;

            if (patchCard.id().isPresent()) {
                Integer cardId = patchCard.id().get();
                HomeInfoCardModel existingCard = homeInfoCardRepository
                    .findById(cardId)
                    .orElse(null);

                if (existingCard == null) {
                    results.put("card_" + cardId, "Card not found");
                    order++;
                    continue;
                }

                boolean infoChanged = checkChangeByInfoCard(existingCard, patchCard);
                boolean orderChanged = existingCard.getOrder() != order;

                boolean hasImageChange = cardImage != null;
                boolean deleteImage = hasImageChange && cardImage.length() == 0;
                boolean replaceImage = hasImageChange && cardImage.length() > 0;

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
                            updateOrCreateCardImage(existingCard, cardImage, category);
                            results.put("card_" + cardId + "_image", "Image updated");
                        } catch (IOException e) {
                            results.put("card_" + cardId + "_image_error", "Failed to process image: " + e.getMessage());
                        }
                    }

                    homeInfoCardRepository.save(existingCard);
                    results.put("card_" + cardId, "Updated successfully");
                } else {
                    results.put("card_" + cardId, "No changes");
                }

                processedCards.put(cardId, true);

            } else {
                HomeInfoCardModel newCard = new HomeInfoCardModel();
                newCard.setTitle(patchCard.title());
                newCard.setDescription(patchCard.description());
                newCard.setColor(patchCard.color());
                newCard.setOrder((byte) order);

                if (cardImage != null && cardImage.length() > 0) {
                    try {
                        updateOrCreateCardImage(newCard, cardImage, category);
                        results.put("card_new_" + order + "_image", "Image added");
                    } catch (IOException e) {
                        results.put("card_new_" + order + "_image_error", "Failed to process image: " + e.getMessage());
                    }
                }

                HomeInfoCardModel savedNewCard = homeInfoCardRepository.save(newCard);
                results.put("card_new_" + order, "Created with ID: " + savedNewCard.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedCards.entrySet()) {
            if (!entry.getValue()) {
                HomeInfoCardModel cardToDelete = homeInfoCardRepository.findById(entry.getKey()).orElse(null);
                if (cardToDelete != null) {
                    deleteCardWithImage(cardToDelete);
                    results.put("card_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    @Override
    public List<HomeInfoCardModel> findAllByConfigVersion(int configId) {
        return homeInfoCardRepository.findAllByConfigVersion(configId);
    }

    private void updateOrCreateCardImage(HomeInfoCardModel card, File imageFile, String category) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        ImageModel currentImage = card.getIcon();

        if (currentImage != null) {
            imageService.saveImageBytes(imageBytes, currentImage.getPath());
        } else {
            UploadImageResponse response = imageService.createAndSaveImageBytes(
                imageBytes,
                category,
                imageFile.getName(),
                "Home info card: " + card.getTitle()
            );
            String path = response.url().substring(response.url().indexOf("/images/") + 1);
            ImageModel newImage = imageService.getImageModelByPath(path);
            card.setIcon(newImage);
        }
    }

    private void deleteImageFromCard(HomeInfoCardModel card) {
        ImageModel oldImage = card.getIcon();
        card.setIcon(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteCardWithImage(HomeInfoCardModel card) {
        ImageModel imageToDelete = card.getIcon();
        if (imageToDelete != null) {
            imageService.deleteImage(imageToDelete.getPath());
        }
        homeInfoCardRepository.deleteById(card.getId());
    }

    private boolean checkChangeByInfoCard(HomeInfoCardModel savedCard, PatchInformationCard updated) {
        return (savedCard.getColor() != updated.color() ||
            !Objects.equals(savedCard.getTitle(), updated.title()) ||
            !Objects.equals(savedCard.getDescription(), updated.description())
        );
    }
}
