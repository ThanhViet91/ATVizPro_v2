package com.atsoft.screenrecord.model;

public class ConfigsModel {
    String feedbackEmail;
    String shareText;
    int interstitialPercent;
    int frequencyCapping;
    String termsURL;
    String privacyPolicyURL;
    int appVersion;

    public ConfigsModel() {
    }

    public String getFeedbackEmail() {
        return feedbackEmail;
    }

    public void setFeedbackEmail(String feedbackEmail) {
        this.feedbackEmail = feedbackEmail;
    }

    public String getShareText() {
        return shareText;
    }

    public void setShareText(String shareText) {
        this.shareText = shareText;
    }

    public int getInterstitialPercent() {
        return interstitialPercent;
    }

    public void setInterstitialPercent(int interstitialPercent) {
        this.interstitialPercent = interstitialPercent;
    }

    public int getFrequencyCapping() {
        return frequencyCapping;
    }

    public void setFrequencyCapping(int frequencyCapping) {
        this.frequencyCapping = frequencyCapping;
    }

    public String getTermsURL() {
        return termsURL;
    }

    public void setTermsURL(String termsURL) {
        this.termsURL = termsURL;
    }

    public String getPrivacyPolicyURL() {
        return privacyPolicyURL;
    }

    public void setPrivacyPolicyURL(String privacyPolicyURL) {
        this.privacyPolicyURL = privacyPolicyURL;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }
}
