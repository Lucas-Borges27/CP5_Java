package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.entities.OutboxEvent;
import br.com.fiap.produtosms.entities.Produto;
import br.com.fiap.produtosms.repositories.OutboxEventRepository;
import br.com.fiap.produtosms.repositories.ProdutoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
class ProdutoServiceImpl implements ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoServiceImpl.class);
    private static final String PRODUTO = "PRODUTO";
    private static final String QUEUE = "produto.queue";

    private final ProdutoRepository repository;
    private final EntityManager entityManager;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Counter produtoSalvoCounter;

    public ProdutoServiceImpl(ProdutoRepository repository,
                              EntityManager entityManager,
                              OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper,
                              MeterRegistry meterRegistry) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        
        // Métrica customizada: contador de produtos salvos
        this.produtoSalvoCounter = Counter.builder("produtos.salvos.total")
                .description("Total de produtos salvos no sistema")
                .register(meterRegistry);
    }

    @Override
    public Produto findById(UUID codigo) {
        logger.debug("Buscando produto codigo={}", codigo);
        return this.repository.findById(codigo)
                .orElseThrow(() -> {
                    logger.warn("Produto nao encontrado codigo={}", codigo);
                    return new NoSuchElementException("Produto nao encontrado");
                });
    }

    @Override
    public List<Produto> findAll() {
        logger.debug("Listando todos os produtos");
        List<Produto> produtos = this.repository.findAll();
        logger.info("Total de produtos encontrados: {}", produtos.size());
        return produtos;
    }

    @Override
    @Transactional
    public void saveOrUpdate(Produto produto) {
        var produtoExistente = this.repository.findById(produto.getCodigo());
        
        if (produtoExistente.isPresent()) {
            var entity = produtoExistente.get();
            logger.info("Atualizando produto codigo={} nome={}", produto.getCodigo(), produto.getNome());
            entity.setNome(produto.getNome());
            entity.setDescricao(produto.getDescricao());
            entity.setPreco(produto.getPreco());
            entity.setCategoria(produto.getCategoria());
        } else {
            logger.info("Criando novo produto codigo={} nome={}", produto.getCodigo(), produto.getNome());
            this.entityManager.persist(produto);
        }
        
        // Incrementar métrica customizada
        produtoSalvoCounter.increment();
        
        // Criar evento outbox na mesma transação
        try {
            String payload = createProdutoPayload(produto);
            OutboxEvent outboxEvent = new OutboxEvent(
                produto.getCodigo().toString(),
                PRODUTO,
                QUEUE,
                payload
            );
            this.outboxEventRepository.save(outboxEvent);
            logger.info("Evento outbox criado produtoId={} queue={}", produto.getCodigo(), QUEUE);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar produto para outbox produtoId={}", produto.getCodigo(), e);
            throw new RuntimeException("Erro ao criar evento de produto", e);
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID codigo) {
        logger.info("Excluindo produto codigo={}", codigo);
        this.repository.deleteById(codigo);
        logger.info("Produto excluido codigo={}", codigo);
    }
    
    private String createProdutoPayload(Produto produto) throws JsonProcessingException {
        // Criar um DTO simples para o payload
        var payload = new ProdutoEventPayload(
            produto.getCodigo(),
            produto.getNome(),
            produto.getCategoria(),
            produto.getPreco()
        );
        return objectMapper.writeValueAsString(payload);
    }
    
    // Record interno para o payload do evento
    private record ProdutoEventPayload(
        UUID codigo,
        String nome,
        String categoria,
        java.math.BigDecimal preco
    ) {}
}
