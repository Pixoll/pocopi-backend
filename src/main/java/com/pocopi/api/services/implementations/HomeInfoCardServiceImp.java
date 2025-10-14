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
    public Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, List<Optional<File>> updateImages) {
        Map<String, String> results = new HashMap<>();
        List<HomeInfoCardModel> allExistingCards = homeInfoCardRepository.findAll();
        Map<Integer, Boolean> processedCards = new HashMap<>();
        String category = "/home/cards";

        for (HomeInfoCardModel card : allExistingCards) {
            processedCards.put(card.getId(), false);
        }

        int order = 0;
        for (PatchInformationCard patchCard : updateInformationCards) {
            Optional<File> imageOptional = (updateImages != null && order < updateImages.size())
                ? updateImages.get(order)
                : Optional.empty();

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

                boolean deleteImage = imageOptional.isPresent() && imageOptional.get().length() == 0;
                boolean replaceImage = imageOptional.isPresent() && imageOptional.get().length() > 0;
                boolean imageUnchanged = imageOptional.isEmpty(); // Optional.empty() = sin cambios

                if (infoChanged || deleteImage || replaceImage) {
                    existingCard.setTitle(patchCard.title());
                    existingCard.setDescription(patchCard.description());
                    existingCard.setColor(patchCard.color());
                    existingCard.setOrder((byte) order);

                    if (deleteImage) {
                        ImageModel oldImage = existingCard.getIcon();
                        existingCard.setIcon(null);
                        if (oldImage != null) {
                            imageService.deleteImage(oldImage.getPath());
                        }
                    } else if (replaceImage) {
                        try {
                            byte[] imageBytes = Files.readAllBytes(imageOptional.get().toPath());
                            ImageModel currentImage = existingCard.getIcon();

                            if (currentImage != null) {
                                imageService.saveImageBytes(imageBytes, currentImage.getPath());
                            } else {
                                UploadImageResponse response = imageService.createAndSaveImageBytes(
                                    imageBytes,
                                    category,
                                    imageOptional.get().getName(),
                                    "Home info card: " + existingCard.getTitle()
                                );
                                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                                ImageModel newImage = imageService.getImageModelByPath(path);
                                existingCard.setIcon(newImage);
                            }
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

                if (imageOptional.isPresent() && imageOptional.get().length() > 0) {
                    try {
                        byte[] imageBytes = Files.readAllBytes(imageOptional.get().toPath());
                        UploadImageResponse response = imageService.createAndSaveImageBytes(
                            imageBytes,
                            category,
                            imageOptional.get().getName(),
                            "Home info card: " + newCard.getTitle()
                        );
                        String path = response.url().substring(response.url().indexOf("/images/") + 1);
                        ImageModel newImage = imageService.getImageModelByPath(path);
                        newCard.setIcon(newImage);
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
                    ImageModel imageToDelete = cardToDelete.getIcon();
                    if (imageToDelete != null) {
                        imageService.deleteImage(imageToDelete.getPath());
                    }
                    homeInfoCardRepository.deleteById(entry.getKey());
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

    private boolean checkChangeByInfoCard(HomeInfoCardModel savedCard, PatchInformationCard updated) {
        return (savedCard.getColor() != updated.color() ||
            !Objects.equals(savedCard.getTitle(), updated.title()) ||
            !Objects.equals(savedCard.getDescription(), updated.description())
        );
    }
}
