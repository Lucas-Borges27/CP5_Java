package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.entities.OutboxEvent;

import java.util.List;

public interface OutBoxService {

    List<OutboxEvent> findPedentesToProcess();

    OutboxEvent save(OutboxEvent outboxEvent);
}
