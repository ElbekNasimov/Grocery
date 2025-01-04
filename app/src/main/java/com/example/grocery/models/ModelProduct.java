package com.example.grocery.models;

public class ModelProduct {
    private String prID, prTitle, prDesc, prCat, prLoc, prQuan, prIcon, prQRCode, prBarCode,
            qrCode, barCode,
            prPrice, prIsReserve, prDiscPrice, prDiscNote, prIsDisc, timestamp, uid;

    public ModelProduct() {
    }

    public ModelProduct(String prID, String prTitle, String prDesc, String prCat, String prLoc, String prQuan, String prIcon,
                        String qrCode, String barCode,
                        String prQRCode, String prBarCode, String prPrice, String prIsReserve, String prDiscPrice,
                        String prDiscNote, String prIsDisc, String timestamp, String uid) {
        this.prID = prID;
        this.prTitle = prTitle;
        this.prDesc = prDesc;
        this.prCat = prCat;
        this.prLoc = prLoc;
        this.prQuan = prQuan;
        this.prIcon = prIcon;
        this.prQRCode = prQRCode;
        this.prBarCode = prBarCode;
        this.prPrice = prPrice;
        this.prIsReserve = prIsReserve;
        this.prDiscPrice = prDiscPrice;
        this.prDiscNote = prDiscNote;
        this.prIsDisc = prIsDisc;
        this.timestamp = timestamp;
        this.uid = uid;

        this.barCode=barCode;
        this.qrCode=qrCode;
    }


    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getPrID() {
        return prID;
    }

    public void setPrID(String prID) {
        this.prID = prID;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getPrDesc() {
        return prDesc;
    }

    public void setPrDesc(String prDesc) {
        this.prDesc = prDesc;
    }

    public String getPrCat() {
        return prCat;
    }

    public void setPrCat(String prCat) {
        this.prCat = prCat;
    }

    public String getPrLoc() {
        return prLoc;
    }

    public void setPrLoc(String prLoc) {
        this.prLoc = prLoc;
    }

    public String getPrQuan() {
        return prQuan;
    }

    public void setPrQuan(String prQuan) {
        this.prQuan = prQuan;
    }

    public String getPrIcon() {
        return prIcon;
    }

    public void setPrIcon(String prIcon) {
        this.prIcon = prIcon;
    }

    public String getPrQRCode() { return prQRCode; }

    public void setPrQRCode(String prQRCode) { this.prQRCode = prQRCode; }
    public String getPrBarCode() { return prBarCode; }
    public void setPrBarCode(String prBarCode) { this.prBarCode = prBarCode; }

    public String getPrPrice() {
        return prPrice;
    }

    public void setPrPrice(String prPrice) {
        this.prPrice = prPrice;
    }
    public String getPrIsReserve() { return prIsReserve; }
    public void setPrIsReserve(String prIsReserve) { this.prIsReserve = prIsReserve; }

    public String getPrDiscPrice() {
        return prDiscPrice;
    }

    public void setPrDiscPrice(String prDiscPrice) {
        this.prDiscPrice = prDiscPrice;
    }

    public String getPrDiscNote() {
        return prDiscNote;
    }

    public void setPrDiscNote(String prDiscNote) {
        this.prDiscNote = prDiscNote;
    }

    public String getPrIsDisc() {
        return prIsDisc;
    }

    public void setPrIsDisc(String prIsDisc) {
        this.prIsDisc = prIsDisc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
