package platformer.bridge;

import platformer.bridge.requests.AccountDataDTO;
import platformer.bridge.requests.BoardItemDTO;
import platformer.core.Account;
import platformer.model.BoardItem;

import java.util.List;
import java.util.stream.Collectors;

// TODO: Refactor
public class Mapper {

    public Account mapToAccount(AccountDataDTO accountDataDTO) {
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

    public AccountDataDTO mapToAccountDataDTO(Account account) {
        return new AccountDataDTO(
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

}
