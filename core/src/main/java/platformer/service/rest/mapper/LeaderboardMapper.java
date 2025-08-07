package platformer.service.rest.mapper;

import platformer.service.rest.requests.BoardItemDTO;
import platformer.model.BoardItem;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LeaderboardMapper implements Mapper<BoardItem, BoardItemDTO>{

    @Override
    public Function<BoardItem, BoardItemDTO> toDto() {
        return null;
    }

    @Override
    public Function<BoardItemDTO, BoardItem> toEntity() {
        return boardItemDTO -> new BoardItem(
                boardItemDTO.getUsername(),
                boardItemDTO.getLevel(),
                boardItemDTO.getExp()
        );
    }

    @Override
    public List<BoardItemDTO> toDtoList(List<BoardItem> entityList) {
        return List.of();
    }

    @Override
    public List<BoardItem> toEntityList(List<BoardItemDTO> dtoList) {
        return dtoList.stream()
                .map(this.toEntity())
                .collect(Collectors.toList());
    }

}
