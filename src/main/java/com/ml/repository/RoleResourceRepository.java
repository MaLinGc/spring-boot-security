package com.ml.repository;

import com.ml.entity.RoleResource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleResourceRepository extends JpaRepository<RoleResource, Long> {

    @EntityGraph(attributePaths = {"role", "resource"})
    List<RoleResource> findByResourceId(Long resourceId);
}
