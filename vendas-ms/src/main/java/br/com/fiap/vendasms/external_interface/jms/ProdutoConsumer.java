package br.com.fiap.vendasms.external_interface.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProdutoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoConsumer.class);

    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final Propagator propagator;

    public ProdutoConsumer(ObjectMapper objectMapper, Tracer tracer, Propagator propagator) {
        this.objectMapper = objectMapper;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    private record ProdutoEventPayload(UUID codigo, String nome, String categoria, BigDecimal preco) {}

    @JmsListener(destination = "produto.queue")
    public void consume(Message message) {
        // Restaurar o contexto de rastreamento a partir das propriedades da mensagem JMS
        Span span = tracer.nextSpan();
        
        try {
            // Extrair headers B3 da mensagem JMS (com '_' ao invés de '-')
            propagator.extract(message, (msg, key) -> {
                try {
                    return msg.getStringProperty(key.replace("-", "_"));
                } catch (Exception e) {
                    logger.debug("Propriedade de trace nao encontrada: {}", key);
                    return null;
                }
            });
            
            span.name("jms.produto.consume")
                .tag("destination", "produto.queue")
                .start();

            try (var ignored = tracer.withSpan(span)) {
                String messageBody = message.getBody(String.class);
                ProdutoEventPayload produto = objectMapper.readValue(messageBody, ProdutoEventPayload.class);
                
                logger.info("Produto recebido da fila codigo={} nome={} categoria={}", 
                    produto.codigo(), produto.nome(), produto.categoria());
                
                // Aqui futuramente poderia haver lógica de negócio
                // Por enquanto apenas registramos o recebimento
                
            } catch (Exception e) {
                logger.error("Erro ao processar mensagem de produto.queue: {}", e.getMessage(), e);
                span.error(e);
            }
            
        } finally {
            span.end();
        }
    }
}
