package dto;

import java.util.List;

public class TokensRequest {
    private List<String> idTokens;

    public List<String> getIdTokens() {
        return idTokens;
    }

    public void setIdTokens(List<String> idTokens) {
        this.idTokens = idTokens;
    }
}
