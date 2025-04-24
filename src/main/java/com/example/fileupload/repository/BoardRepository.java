package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;

import jakarta.transaction.Transactional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>{
	List<BoardMapping> findAllBy();	// 원하는 컬럼만 반환하기 위해 맵핑 타입을 사용
	
	Page<BoardMapping> findByTitleContaining(Pageable pageable,String word);	// 원하는 컬럼만 반환하기 위해 맵핑 타입을 사용
	BoardMapping findByBno(int bno);
	
	@Modifying			// 수정
	@Transactional		// 트랜잭션 롤백
	@Query(nativeQuery = true,
			value="UPDATE board SET "
					+ " title = :title "
					+ " WHERE bno = :bno AND pw = :pw")
	void modifyByTitle(int bno, String title, String pw);
}
