package com.pocopi.api.repositories;

import com.pocopi.api.models.config.ImageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageModel, Integer> {
    Optional<ImageModel> findByPath(String path);

    @NativeQuery(
        """
            select count(i.id)
                from image                         i
                    left join config               c on c.icon_id = i.id
                    left join home_info_card       hc on hc.icon_id = i.id
                    left join form_question        fq on fq.image_id = i.id
                    left join form_question_option fo on fo.image_id = i.id
                    left join test_question        tq on tq.image_id = i.id
                    left join test_option          o on o.image_id = i.id
                where i.id = :imageId
                  and (c.icon_id = i.id
                    or hc.icon_id = i.id
                    or fq.image_id = i.id
                    or fo.image_id = i.id
                    or tq.image_id = i.id
                    or o.image_id = i.id
                    )
            """
    )
    long countImageUsages(int imageId);

    default boolean isImageUsed(int imageId) {
        return countImageUsages(imageId) > 0;
    }
}
