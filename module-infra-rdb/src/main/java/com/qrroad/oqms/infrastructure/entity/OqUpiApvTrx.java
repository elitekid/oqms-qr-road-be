package com.qrroad.oqms.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "oq_upi_apv_trx", schema = "qrbgw")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OqUpiApvTrx {
    @EmbeddedId
    private OqUpiApvTrxPK oqUpiApvTrxPK;

    @Column(name = "msg_tp", nullable = false, length = 4)
    private String msgTp;

    @Column(name = "pan", nullable = false, length = 2048)
    private String pan;

    @Column(name = "proc_cd", nullable = false, length = 6)
    private String procCd;

    @Column(name = "tr_amt", nullable = false, length = 12)
    private String trAmt;

    @Column(name = "settle_amt", length = 12)
    private String settleAmt;

    @Column(name = "bill_amt", length = 12)
    private String billAmt;

    @Column(name = "tr_date_time", nullable = false, length = 10)
    private String trDateTime;

    @Column(name = "conv_rate_settle", length = 8)
    private String convRateSettle;

    @Column(name = "conv_rate_bill", length = 10)
    private String convRateBill;

    @Column(name = "stan", length = 6)
    private String stan;

    @Column(name = "local_time", nullable = false, length = 6)
    private String localTime;

    @Column(name = "local_date", nullable = false, length = 4)
    private String localDate;

    @Column(name = "exp_date", length = 4)
    private String expDate;

    @Column(name = "settle_date", length = 4)
    private String settleDate;

    @Column(name = "conv_date", length = 4)
    private String convDate;

    @Column(name = "merchant_type", length = 4)
    private String merchantType;

    @Column(name = "merchant_country", length = 3)
    private String merchantCountry;

    @Column(name = "pos_entry_mode", length = 3)
    private String posEntryMode;

    @Column(name = "card_seq_no", length = 3)
    private String cardSeqNo;

    @Column(name = "pos_cond_code", length = 2)
    private String posCondCode;

    @Column(name = "pos_pin_code", length = 2)
    private String posPinCode;

    @Column(name = "trans_fee_amt", length = 8)
    private String transFeeAmt;

    @Column(name = "acq_inst_code", length = 11)
    private String acqInstCode;

    @Column(name = "fwd_inst_code", length = 11)
    private String fwdInstCode;

    @Column(name = "track2_data", length = 37)
    private String track2Data;

    @Column(name = "track3_data", length = 104)
    private String track3Data;

    @Column(name = "rrn", nullable = false, length = 12)
    private String rrn;

    @Column(name = "auth_resp_code", length = 6)
    private String authRespCode;

    @Column(name = "resp_code", length = 2)
    private String respCode;

    @Column(name = "term_id", length = 8)
    private String termId;

    @Column(name = "acceptor_id", length = 15)
    private String acceptorId;

    @Column(name = "acceptor_name_loc", length = 40)
    private String acceptorNameLoc;

    @Column(name = "addl_resp_data", length = 25)
    private String addlRespData;

    @Column(name = "track1_data", length = 76)
    private String track1Data;

    @Column(name = "addl_data_priv", length = 512)
    private String addlDataPriv;

    @Column(name = "curr_code_trans", length = 3)
    private String currCodeTrans;

    @Column(name = "curr_code_settle", length = 3)
    private String currCodeSettle;

    @Column(name = "curr_code_bill", length = 3)
    private String currCodeBill;

    @Column(name = "pin_data", length = 8)
    private String pinData;

    @Column(name = "sec_control_info", length = 16)
    private String secControlInfo;

    @Column(name = "addl_amts", length = 40)
    private String addlAmts;

    @Column(name = "ic_card_data", length = 255)
    private String icCardData;

    @Column(name = "token_ref", length = 512)
    private String tokenRef;

    @Column(name = "issuer_addl_data", length = 100)
    private String issuerAddlData;

    @Column(name = "self_defined_field", length = 100)
    private String selfDefinedField;

    @Column(name = "cardholder_auth", length = 200)
    private String cardholderAuth;

    @Column(name = "switch_data", length = 200)
    private String switchData;

    @Column(name = "fin_net_data", length = 512)
    private String finNetData;

    @Column(name = "network_info_code", length = 3)
    private String networkInfoCode;

    @Column(name = "orig_data", length = 42)
    private String origData;

    @Column(name = "msg_sec_code", length = 8)
    private String msgSecCode;

    @Column(name = "recv_inst_code", length = 11)
    private String recvInstCode;

    @Column(name = "acct_id_1", length = 28)
    private String acctId1;

    @Column(name = "acct_id_2", length = 28)
    private String acctId2;

    @Column(name = "trans_ind_info", length = 512)
    private String transIndInfo;

    @Column(name = "addl_info", length = 512)
    private String addlInfo;

    @Column(name = "national_info", length = 256)
    private String nationalInfo;

    @Column(name = "gscs_reserved", length = 100)
    private String gscsReserved;

    @Column(name = "acq_reserved", length = 100)
    private String acqReserved;

    @Column(name = "issuer_reserved", length = 100)
    private String issuerReserved;

    @Column(name = "sec_risk_info", length = 256)
    private String secRiskInfo;

    @Column(name = "mac", length = 8)
    private String mac;

    @Column(name = "wb_resp_code", length = 2)
    private String wbRespCode;

    @Column(name = "wb_resp_msg", length = 255)
    private String wbRespMsg;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "create_time")
    private Timestamp createTime;
}