package com.example.fileupload.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class BoardfileController {
	
	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	// 삭제
	@GetMapping("/removeBoardfile")
	public String removeBoardfile(
				@RequestParam(value = "fno") int fno
				,@RequestParam(value = "bno") int bno) {
		
		// 파일 삭제 후
		Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);
		File file = new File("c:/project/upload/" + boardfile.getFname() + "." + boardfile.getFext());
		
		if(file.exists()) {
			file.delete();
		}
		
		// 테이블 Row 삭제
		boardfileRepository.deleteById(fno);
		return "redirect:/boardOne?bno="+bno;
	}
	
	// 입력폼
	@GetMapping("/addBoard")
	public String addBoard() {
		return "addBoard";
	}
	
	// 입력액션
	@PostMapping("/addBoard")
	@Transactional
	public String addBoard(BoardForm boardForm, RedirectAttributes rda) {
		log.debug(boardForm.toString());
		// 이슈 : 파일을 첨부하지 않아도 fileSize는 1 이다
		log.debug("boardForm.getFileList().size() : "+boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board);	// board 저장
		
		int bno = board.getBno();	// board insert 후 bno 변경되었는지
		log.debug("bno : " + bno);
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firstFileSize = fileList.get(0).getSize();
		log.debug(""+firstFileSize);
		
		// 이슈 : 파일을 첨부하지 않아도 fileSize는 1 이다
		if(firstFileSize > 0) {	// 첫번째 파일 사이즈가 0이상이다 -> 첨부된 파일이 있다.
			
			for(MultipartFile f : fileList) {
				if(f.getContentType().equals("application/octet-stream") || f.getSize() > 1024*1024*10) {	// 1KB = 1024byte, 1MB = 1KB * 1024byte
					rda.addFlashAttribute("msg","첨부할 수 없는 파일 형식 입니다.");
					return "redirect:/addBoard";
				}
			}
			
			for(MultipartFile f : fileList) {
				log.debug("파일 타입 : "+f.getContentType());
				log.debug("파일 이름 : "+f.getName());
				log.debug("원본 이름 : "+f.getOriginalFilename());
				log.debug("파일 용량 : "+f.getSize());
				
				// 확장자 추출 후 따로 저장
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".") + 1);
				log.debug("확장자 : " + ext);
				String saveName = UUID.randomUUID().toString().replace("-","");
				log.debug("저장파일이름 : " + saveName);
				
				File emptyFile = new File("c:/project/upload/" + saveName + "." + ext);
				// f의 byte -> emptyFile 복사
				try {
					f.transferTo(emptyFile);
				} catch (Exception e) {
					log.error("파일 저장 실패!");
					e.printStackTrace();
				}
				
				// boardfile테이블에도 파일정보 저장
				Boardfile boardfile = new Boardfile();
				//boardfile.setBoard(board);
				boardfile.setBno(board.getBno());
				boardfile.setFext(ext);
				boardfile.setFname(saveName);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				
				boardfileRepository.save(boardfile);
			}
		}
		
		return "redirect:/boardList";
	}
}
