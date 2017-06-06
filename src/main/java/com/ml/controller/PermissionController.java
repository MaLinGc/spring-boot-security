package com.ml.controller;

import com.ml.security.CustomSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private CustomSecurityMetadataSource customSecurityMetadataSource;

    @RequestMapping("/update")
    public ResponseEntity<String> updatePermission() {
        customSecurityMetadataSource.refresh();
        return ResponseEntity.ok().build();
    }
}
