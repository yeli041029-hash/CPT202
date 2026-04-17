package com.group32.cpt202.LY_heritage.repository;

import com.group32.cpt202.LY_heritage.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByHeritageIdOrderBySentAtDesc(Long heritageId);
}