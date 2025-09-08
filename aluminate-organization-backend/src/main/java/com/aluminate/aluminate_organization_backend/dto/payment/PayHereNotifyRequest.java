package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayHereNotifyRequest {
    private String merchant_id;
    private String order_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String custom_1; // We'll use this for donation ID
    private String custom_2; // We'll use this for campaign ID
    private String method;
    private String status_message;
    private String card_holder_name;
    private String card_no;
    private String card_expiry;

    // Static builder method to replace @Builder annotation
    public static PayHereNotifyRequestBuilder builder() {
        return new PayHereNotifyRequestBuilder();
    }

    public static class PayHereNotifyRequestBuilder {
        private String merchant_id;
        private String order_id;
        private String payhere_amount;
        private String payhere_currency;
        private String status_code;
        private String md5sig;
        private String custom_1;
        private String custom_2;
        private String method;
        private String status_message;
        private String card_holder_name;
        private String card_no;
        private String card_expiry;

        public PayHereNotifyRequestBuilder merchant_id(String merchant_id) {
            this.merchant_id = merchant_id;
            return this;
        }

        public PayHereNotifyRequestBuilder order_id(String order_id) {
            this.order_id = order_id;
            return this;
        }

        public PayHereNotifyRequestBuilder payhere_amount(String payhere_amount) {
            this.payhere_amount = payhere_amount;
            return this;
        }

        public PayHereNotifyRequestBuilder payhere_currency(String payhere_currency) {
            this.payhere_currency = payhere_currency;
            return this;
        }

        public PayHereNotifyRequestBuilder status_code(String status_code) {
            this.status_code = status_code;
            return this;
        }

        public PayHereNotifyRequestBuilder md5sig(String md5sig) {
            this.md5sig = md5sig;
            return this;
        }

        public PayHereNotifyRequestBuilder custom_1(String custom_1) {
            this.custom_1 = custom_1;
            return this;
        }

        public PayHereNotifyRequestBuilder custom_2(String custom_2) {
            this.custom_2 = custom_2;
            return this;
        }

        public PayHereNotifyRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public PayHereNotifyRequestBuilder status_message(String status_message) {
            this.status_message = status_message;
            return this;
        }

        public PayHereNotifyRequestBuilder card_holder_name(String card_holder_name) {
            this.card_holder_name = card_holder_name;
            return this;
        }

        public PayHereNotifyRequestBuilder card_no(String card_no) {
            this.card_no = card_no;
            return this;
        }

        public PayHereNotifyRequestBuilder card_expiry(String card_expiry) {
            this.card_expiry = card_expiry;
            return this;
        }

        public PayHereNotifyRequest build() {
            return new PayHereNotifyRequest(
                    merchant_id, order_id, payhere_amount, payhere_currency,
                    status_code, md5sig, custom_1, custom_2, method,
                    status_message, card_holder_name, card_no, card_expiry
            );
        }
    }
}