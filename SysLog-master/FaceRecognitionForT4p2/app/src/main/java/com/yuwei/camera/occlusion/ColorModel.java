package com.yuwei.camera.occlusion;

public class ColorModel {

    public int color;
    public int count;
    public int total;

    public ColorModel(int color, int count ,int total) {
        this.color = color;
        this.count = count;
        this.total = total;
    }

    public ColorModel(int color, int count) {
        this.color = color;
        this.count = count;
    }

//    public String percentage() {
//        NumberFormat numberFormat = NumberFormat.getInstance();
//        numberFormat.setMaximumFractionDigits(2);
//        String result = numberFormat.format((float) count / (float) total * 100);
//        return result + "%";
//    }
}
