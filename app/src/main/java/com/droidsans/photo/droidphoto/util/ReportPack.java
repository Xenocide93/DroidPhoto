package com.droidsans.photo.droidphoto.util;

/**
 * Created by Ong on 06/08/2015.
 */
public class ReportPack {
    public String type, build_device, build_model,
            retail_vendor, retail_model, reason, report_date;
    public int photo_id, user_id, report_by;
    public boolean isSevere;

    public ReportPack(String build_device, String build_model, String retail_vendor, String retail_model, String report_date, int report_by) {
        this.type = "device";
        this.build_device = build_device;
        this.build_model = build_model;
        this.retail_vendor = retail_vendor;
        this.retail_model = retail_model;
        this.report_date = report_date;
        this.report_by = report_by;
    }

    public ReportPack(int photo_id, String reason, String report_date, boolean isSevere) {
        this.type = "photo";
        this.photo_id = photo_id;
        this.reason = reason;
        this.report_date = report_date;
        this.isSevere = isSevere;
    }

    public ReportPack(int user_id, String reason, String report_date) {
        this.type = "user";
        this.user_id = user_id;
        this.reason = reason;
        this.report_date = report_date;
    }
}
