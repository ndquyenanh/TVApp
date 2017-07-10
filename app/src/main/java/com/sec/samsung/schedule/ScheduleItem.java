package com.sec.samsung.schedule;

/**
 * Created by sev_user on 11/28/2016.
 */

public class ScheduleItem {
    private int scheduleId;

    private String fileExtension;
    private int fileId;
    private String fileName;
    private String folderName;

    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;

    private int priority;

    public ScheduleItem(int scheduleId, String startDate, String endDate, String startTime, String endTime) {
        this.scheduleId = scheduleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ScheduleItem(int scheduleId,
                        String fileExtension, int fileId, String fileName, String folderName,
                        String startDate, String endDate, String startTime, String endTime, int priority) {
        this.scheduleId = scheduleId;
        this.fileExtension = fileExtension;
        this.fileId = fileId;
        this.fileName = fileName;
        this.folderName = folderName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public int getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getPriority() {
        return priority;
    }
}
