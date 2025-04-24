package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.Boardfile;

import jakarta.transaction.Transactional;

@Repository
public interface BoardfileRepository extends JpaRepository<Boardfile, Integer>{
	List<Boardfile> findByBno(int bno);
	
	// PK 한행 삭제
	// void deleteById(int id)
	
	// FK 여러행 삭제(Board 삭제 시 같이 삭제 : 트랜잭션)
	
	@Modifying		// 수정 작업시 붙여야 함
	@Transactional	// 메서드 실행 중 문제가 생기면 자동으로 롤백
	@Query(nativeQuery = true
			,value = "DELETE FROM boardfile"
					+ " WHERE bno = :bno")
	void deleteByBno(int bno);
}
