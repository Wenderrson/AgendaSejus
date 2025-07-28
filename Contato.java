package agenda;

public class Contato {

    private String id;
    private String unidade;
    private String contato;
    private String email;
    private String endereco;
    private String categoria;

    public Contato() {
    }

    public Contato(String id, String unidade, String contato, String email, String endereco, String categoria) {
        this.id = id;
        this.unidade = unidade;
        this.contato = contato;
        this.email = email;
        this.endereco = endereco;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCategoria() {

        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

}
