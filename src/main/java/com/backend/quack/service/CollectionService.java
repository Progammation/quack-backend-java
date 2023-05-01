package com.backend.quack.service;

import com.backend.quack.model.CollectionModel;
import com.backend.quack.repository.CollectionRepository;
import com.backend.quack.request.CollectionPostRequestBody;
import com.backend.quack.request.CollectionPutRequestBody;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class CollectionService {
    private final CollectionRepository collectionRepository;

    private static String slugify(String name) {
        String slug = name.replaceAll("[^a-zA-Z0-9\\s]", "");
        slug = slug.toLowerCase();
        slug = slug.replaceAll("\\s", "-");
        return slug;
    }

    public List<CollectionModel> findAllCollections() {
        return collectionRepository.findAll();
    }

    public CollectionModel findCollectionBySlug(String slug) {
        return collectionRepository.findBySlug(slug);
    }

    public CollectionModel findCollectionById(Long id) {
        return collectionRepository.findById(id).orElseThrow();
    }

    public CollectionModel saveCollection(CollectionPostRequestBody collectionPostRequestBody) {
        CollectionModel collectionBySlug = collectionRepository.findBySlug(slugify(collectionPostRequestBody.getName()));

        CollectionModel collection = CollectionModel.builder()
                .name(collectionPostRequestBody.getName())
                .slug(slugify(collectionPostRequestBody.getName()))
                .bio(collectionPostRequestBody.getBio())
                .visibility(collectionPostRequestBody.getVisibility())
                .build();

        if(collectionBySlug != null) {
            Random random = new Random();
            collection.setSlug(collection.getSlug() + "-" + random.nextInt(10000));
        }

        return collectionRepository.save(collection);
    }

    public CollectionModel updateCollection(Long id, CollectionPutRequestBody collectionPutRequestBody) {
        Optional<CollectionModel> savedCollection = Optional.ofNullable(collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection not Found")));

        CollectionModel collection = CollectionModel.builder()
                .id(savedCollection.get().getId())
                .name(savedCollection.get().getName())
                .slug(savedCollection.get().getSlug())
                .bio(savedCollection.get().getBio())
                .visibility(savedCollection.get().getVisibility())
                .build();

        if (collectionPutRequestBody.getName() != null && !collectionPutRequestBody.getName().equals(collection.getName())) {
            collection.setName(collectionPutRequestBody.getName());
            collection.setSlug(slugify(collectionPutRequestBody.getName()));

            CollectionModel collectionBySlug = collectionRepository.findBySlug(slugify(collectionPutRequestBody.getName()));

            if(collectionBySlug != null && !collectionBySlug.getId().equals(id)) {
                Random random = new Random();
                collection.setSlug(collection.getSlug() + "-" + random.nextInt(10000));
            }
        }

        if(collectionPutRequestBody.getBio() != null) {
            collection.setBio(collectionPutRequestBody.getBio());
        }

        if(collectionPutRequestBody.getVisibility() != null) {
            collection.setVisibility(collectionPutRequestBody.getVisibility());
        }

        return collectionRepository.save(collection);
    }

    public void deleteCollection(Long id) {
        collectionRepository.deleteById(id);
    }
}
