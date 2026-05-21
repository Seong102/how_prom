package com.howprom.controller.submission;


import com.howprom.submission.dto.MyPageResponseDto;
import com.howprom.submission.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage") // 👈 앞에 /api가 붙어있다면 반드시 지우세요!
@RequiredArgsConstructor
public class MyPageViewController {

    private final MyPageService myPageService;

    @GetMapping // 👈 호출 주소는 최종적으로 [GET] /mypage 가 됩니다.
    public String getMyPageHTML(Pageable pageable, Model model) {

        Long mockUserId = 2L;
        MyPageResponseDto myPageData = myPageService.getMyPageData(mockUserId, pageable);

        model.addAttribute("stats", myPageData.getStats());
        model.addAttribute("submissions", myPageData.getSubmissions().getContent());
        model.addAttribute("page", myPageData.getSubmissions());
        model.addAttribute("username", "userA");
        return "mypage/mypage"; // templates/mypage.html 반환
    }
}