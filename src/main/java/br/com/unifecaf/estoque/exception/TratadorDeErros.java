package br.com.unifecaf.estoque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Converte excecoes em respostas JSON padronizadas para o cliente da API.
 */
@RestControllerAdvice
public class TratadorDeErros {

    public record Erro(LocalDateTime timestamp, int status, String erro, List<String> detalhes) {
        static Erro de(HttpStatus s, List<String> detalhes) {
            return new Erro(LocalDateTime.now(), s.value(), s.getReasonPhrase(), detalhes);
        }
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, List.of(e.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Erro> conflito(RegraNegocioException e) {
        return resposta(HttpStatus.CONFLICT, List.of(e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Erro> invalido(RuntimeException e) {
        return resposta(HttpStatus.BAD_REQUEST, List.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, detalhes);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Erro> formato(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, List.of("Requisicao em formato invalido. Verifique os campos enviados."));
    }

    private ResponseEntity<Erro> resposta(HttpStatus s, List<String> detalhes) {
        return ResponseEntity.status(s).body(Erro.de(s, detalhes));
    }
}
