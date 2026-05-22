alter table test_group
    add column greeting_image_id int unsigned null references image (id) after greeting;
