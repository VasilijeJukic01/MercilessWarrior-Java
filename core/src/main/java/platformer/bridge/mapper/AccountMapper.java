package platformer.bridge.mapper;

import platformer.bridge.requests.AccountDataDTO;
import platformer.bridge.requests.BoardItemDTO;
import platformer.core.Account;
import platformer.model.BoardItem;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccountMapper implements Mapper<Account, AccountDataDTO> {

    @Override
    public Function<AccountDataDTO, Account> toEntity() {
        return accountDataDTO -> {
            Account account = new Account(
                    accountDataDTO.getUsername(),
                    "",
                    accountDataDTO.getAccountId(),
                    accountDataDTO.getSettingsId(),
                    accountDataDTO.getSpawn(),
                    accountDataDTO.getCoins(),
                    accountDataDTO.getTokens(),
                    accountDataDTO.getLevel(),
                    accountDataDTO.getExp()
            );
            account.setItems(accountDataDTO.getItems());
            account.setPerks(accountDataDTO.getPerks());
            return account;
        };
    }

    @Override
    public Function<Account, AccountDataDTO> toDto() {
        return account -> new AccountDataDTO(
                account.getName(),
                account.getAccountID(),
                account.getSettingsID(),
                account.getSpawn(),
                account.getCoins(),
                account.getTokens(),
                account.getLevel(),
                account.getExp(),
                account.getPerks(),
                account.getItems()
        );
    }

    @Override
    public List<AccountDataDTO> toDtoList(List<Account> entityList) {
        return entityList.stream()
                .map(this.toDto())
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> toEntityList(List<AccountDataDTO> dtoList) {
        return dtoList.stream()
                .map(this.toEntity())
                .collect(Collectors.toList());
    }

    public List<BoardItem> mapToBoardItem(List<BoardItemDTO> boardItemDTOs) {
        return boardItemDTOs.stream()
                .map(boardItemDTO -> new BoardItem(
                        boardItemDTO.getUsername(),
                        boardItemDTO.getLevel(),
                        boardItemDTO.getExp()
                ))
                .collect(Collectors.toList());
    }

}
