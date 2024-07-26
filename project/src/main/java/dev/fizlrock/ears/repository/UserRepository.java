package dev.fizlrock.ears.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import dev.fizlrock.ears.domain.entities.User;


@Repository
public interface UserRepository
    extends PagingAndSortingRepository<User, Long>, CrudRepository<User,Long> {

  Optional<User> findByUsername(String username);

}
