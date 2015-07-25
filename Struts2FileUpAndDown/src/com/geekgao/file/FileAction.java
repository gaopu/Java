package com.geekgao.file;

import com.opensymphony.xwork2.ActionSupport;

import java.io.*;

public class FileAction extends ActionSupport {

    private File file;
    private String fileFileName;
    private String fileContentType;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileFileName() {
        return fileFileName;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String upFile() throws IOException {
        if (file == null) {
            return INPUT;
        }

        FileInputStream inFile = new FileInputStream(file);
        FileOutputStream outFle = new FileOutputStream(new File("/home/geekgao/" + fileFileName));
        byte[] b = new byte[8192];
        int bLength;

        while (-1 != (bLength = inFile.read(b))) {
            outFle.write(b,0,bLength);
        }
        inFile.close();
        outFle.close();
        return SUCCESS;
    }

    public String downFile() {
        return SUCCESS;
    }
}
