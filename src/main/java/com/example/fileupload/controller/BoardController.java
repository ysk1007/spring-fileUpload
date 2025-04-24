package com.example.fileupload.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class BoardController {
	
	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	// 보드 수정 폼
	@GetMapping({"modifyBoard"})
	public String modifyBoard(Model model, @RequestParam int bno) {
		model.addAttribute("bno",bno);
		
		BoardMapping board = boardRepository.findByBno(bno);
		model.addAttribute("title", board.getTitle());
		
		return "modifyBoard";
	}
	
	// 보드 수정 액션
	@PostMapping({"modifyBoard"})
	public String modifyBoard(RedirectAttributes rda, BoardForm boardform, @RequestParam int bno) {
		
		Board board = boardRepository.findById(bno).orElse(null);
		
		log.debug("board 비밀번호 : " + board.getPw());
		log.debug("입력 비밀번호 : " + boardform.getPw());
		
		// 비밀번호 틀림
		if(!board.getPw().equals(boardform.getPw())) {
			rda.addFlashAttribute("msg", "수정 실패!");
			return "redirect:/modifyBoard?bno="+bno;
		}
		
		log.debug("수정할 내용 : " + boardform.toString());
		boardRepository.modifyByTitle(bno,boardform.getTitle(),boardform.getPw());
		
		return "redirect:/boardOne?bno="+bno;
	}
	
	// 보드 삭제 폼
	@GetMapping({"removeBoard"})
	public String removeBoard(Model model, @RequestParam int bno) {
		model.addAttribute("bno",bno);
		return "removeBoard";
	}

	
	// 보드 삭제 액션
	@PostMapping({"removeBoard"})
	public String removeBoard(RedirectAttributes rda, BoardForm boardform, @RequestParam int bno) {
		
		Board board = boardRepository.findById(bno).orElse(null);
		
		log.debug("board 비밀번호 : " + board.getPw());
		log.debug("입력 비밀번호 : " + boardform.getPw());
		
		// 비밀번호 틀림
		if(!board.getPw().equals(boardform.getPw())) {
			rda.addFlashAttribute("msg", "삭제 실패!");
			return "redirect:/removeBoard?bno="+bno;
		}
		
		
		boardfileRepository.deleteByBno(bno);
		boardRepository.delete(board);
		
		return "redirect:/";
	}
	
	// 보드 리스트
	@GetMapping({"/","/boardList"})
	public String boardList(Model model
			, @RequestParam(value="currentPage",defaultValue="0") int currentPage
			, @RequestParam(value="rowPerPage",defaultValue="8") int rowPerPage
			, @RequestParam(value="word",defaultValue="") String word) {
		
		// 페이징
		// Sort 
		Sort sort = Sort.by("bno").descending();
		// PageRequest
		PageRequest page = PageRequest.of(currentPage,rowPerPage,sort);
		
		Page<BoardMapping> list = boardRepository.findByTitleContaining(page,word);
		model.addAttribute("list",list);
		
		model.addAttribute("nextPage",list.getNumber() + 1);
		model.addAttribute("prePage",list.getNumber() - 1);
		model.addAttribute("currentPage",list.getNumber());
		model.addAttribute("hasNext",list.hasNext());
		model.addAttribute("isFirst",list.isFirst());
		model.addAttribute("word",word);
		
		return "boardList";
	}
	
	// 보드 상세
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam(value = "bno") int bno) {
		BoardMapping boardMapping = boardRepository.findByBno(bno);

		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		log.debug("size:"+fileList.size());
		
		model.addAttribute("boardMapping", boardMapping);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
}
