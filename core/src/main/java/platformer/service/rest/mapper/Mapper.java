package platformer.service.rest.mapper;

import java.util.List;
import java.util.function.Function;

/**
 * Mapper interface for mapping entities to DTOs and vice versa.
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface Mapper<E, D> {

    /**
     * Maps an entity to a DTO.
     *
     * @return Function that maps an entity to a DTO
     */
    Function<E, D> toDto();

    /**
     * Maps a DTO to an entity.
     *
     * @return Function that maps a DTO to an entity
     */
    Function<D, E> toEntity();

    /**
     * Maps a list of entities to a list of DTOs.
     *
     * @param entityList List of entities
     * @return List of DTOs
     */
    List<D> toDtoList(List<E> entityList);

    /**
     * Maps a list of DTOs to a list of entities.
     *
     * @param dtoList List of DTOs
     * @return List of entities
     */
    List<E> toEntityList(List<D> dtoList);
}
