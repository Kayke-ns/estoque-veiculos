package br.com.unifecaf.estoque.exception;

/** Lancada quando uma operacao viola uma regra de negocio. Vira HTTP 409. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
