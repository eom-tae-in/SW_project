package com.example.sheetmusiclist.service.sheetmusic;


import com.amazonaws.services.s3.AmazonS3;
import com.example.sheetmusiclist.dto.sheetmusic.*;
import com.example.sheetmusiclist.entity.pdf.Pdf;
import com.example.sheetmusiclist.entity.member.Member;
import com.example.sheetmusiclist.entity.sheetmusic.SheetMusic;
import com.example.sheetmusiclist.exception.MemberNotEqualsException;
import com.example.sheetmusiclist.exception.SheetMusicNotFoundException;
import com.example.sheetmusiclist.repository.pdf.PdfRepository;
import com.example.sheetmusiclist.repository.sheetmusic.SheetMusicRepository;
import com.example.sheetmusiclist.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
@PropertySource("classpath:pdf.properties")
public class SheetMusicService {

    private final SheetMusicRepository sheetMusicRepository;

    private final FileService fileService;


    private final ResourceLoader resourceLoader;

    private final PdfRepository pdfRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;


    public void downloadS3Object(String s3Url) throws IOException {
        Resource resource = resourceLoader.getResource(s3Url);
        File downloadedS3Object = new File(resource.getFilename());

        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, downloadedS3Object.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ?????? ??????
    @Transactional
    public void createSheetMusic(SheetMusicCreateRequestDto req, Member member) {
        List<Pdf> pdfs = req.getPdfs().stream().map(i -> new Pdf(i.getOriginalFilename())).collect(toList());
        SheetMusic sheetMusic = sheetMusicRepository.save(new SheetMusic(member, req.getTitle(), req.getWriter(), pdfs));
        uploadPdfs(sheetMusic.getPdfs(), req.getPdfs());

    }

    // ?????? ?????? ?????????  ??????????????? ???????????????
    @Transactional(readOnly = true)
    public List<SheetMusicFindAllResponseDto> findAllSheetMusic(Pageable pageable) {

        Page<SheetMusic> sheetMusics = sheetMusicRepository.findAll(pageable);

        List<SheetMusicFindAllResponseDto> result = new ArrayList<>();

        for (SheetMusic sheetMusic : sheetMusics) {
            result.add(SheetMusicFindAllResponseDto.toDto(sheetMusic));
        }

        return result;
    }

    // ?????? ?????? ??????
    @Transactional(readOnly = true)
    public SheetMusicFindResponseDto findSheetMusic(Long id) {

        SheetMusic sheetMusic = sheetMusicRepository.findById(id).orElseThrow(SheetMusicNotFoundException::new);

        return SheetMusicFindResponseDto.toDto(sheetMusic);
    }

    //?????? ???????????? ?????? ??????
    @Transactional(readOnly = true)
    public List<SheetMusicSearchResponseDto> searchTitleSheetMusic(Pageable pageable, String title) {
        Page<SheetMusic> sheetMusics = sheetMusicRepository.findAllByTitleContaining(title,pageable);
        List<SheetMusicSearchResponseDto> result = new ArrayList<>();
        sheetMusics.forEach(s -> result.add(SheetMusicSearchResponseDto.toDto(s)));
        return result;
    }

    //?????? ???????????? ?????? ??????
    @Transactional(readOnly = true)
    public List<SheetMusicSearchResponseDto> searchWriterSheetMusic(Pageable pageable,String writer) {
        Page<SheetMusic> sheetMusics = sheetMusicRepository.findAllByWriterContaining(writer,pageable);
        List<SheetMusicSearchResponseDto> result = new ArrayList<>();
        sheetMusics.forEach(s -> result.add(SheetMusicSearchResponseDto.toDto(s)));
        return result;
    }


    // ?????? ??????
    @Transactional
    public void editSheetMusic(Long id, Member member, SheetMusicEditRequestDto req) {

        SheetMusic sheetMusic = sheetMusicRepository.findById(id).orElseThrow(SheetMusicNotFoundException::new);
        if (!sheetMusic.getMember().equals(member)) {
            // ????????? ??????(member)??? ??? ?????? ????????? ??????sheetMusic.getMember()?????? ????????? ????????? ??????
            throw new MemberNotEqualsException(); 
        }
        SheetMusic.PdfUpdatedResult result = sheetMusic.update(req);
        uploadPdfs(result.getAddedPdfs(), result.getAddedPdfFiles());
        deletePdfs(result.getDeletedPdfs());
    }

    // ?????? ??????
    @Transactional
    public void deleteSheetMusic(Long id, Member member) {
        SheetMusic sheetMusic = sheetMusicRepository.findById(id).orElseThrow(SheetMusicNotFoundException::new);

        if (!sheetMusic.getMember().getEmail().equals(member.getEmail())) {
            throw new MemberNotEqualsException();
        }
        List<Pdf> pdfs = pdfRepository.findAllBySheetMusic(sheetMusic);

        deletePdfs(pdfs);
        sheetMusicRepository.deleteById(id);
    }

    //requestDto??? ????????? pdf??? multipartfiles?????? ?????? stream?????? Pdf???????????? ????????? ????????????
    //????????? Pdf(i.getOriginalFilename()) ????????? ?????? Pdf ????????? ????????? ????????? ?????? fileService.upload ????????????
    // ?????????????????? ????????? pdf??? ????????? ?????? pdf???, ??? ????????? ?????? stream?????? ???????????? ??? ?????? uniquefilename??? ?????????
    // ??? ?????? ????????????.
    private void uploadPdfs(List<Pdf> pdfs, List<MultipartFile> filePdfs) {
        IntStream.range(0, pdfs.size()).forEach(i -> fileService.upload(filePdfs.get(i), pdfs.get(i).getUniqueName()));
    }

    private void deletePdfs(List<Pdf> pdfs) {
        pdfs.stream().forEach(i -> fileService.delete(i.getUniqueName()));
    }


}







