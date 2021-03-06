package com.netease.backend.tcc.demo.impl;

import com.netease.backend.tcc.DefaultParticipant;
import com.netease.backend.tcc.demo.IOrder;
import com.netease.backend.tcc.error.ParticipantException;


public class Order extends DefaultParticipant implements IOrder {
	
	private Payment payment;
	private Sale sale;

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public Sale getSale() {
		return sale;
	}

	public void setSale(Sale sale) {
		this.sale = sale;
	}

	public Order() {
		try {
			initMethods("com.netease.backend.tcc.demo.IOrder");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void available(String person, int count) {
		LogUtil.log(person + " buys " + count + " items");
	}

	@Override
	public void cancel(Long uuid) throws ParticipantException {
		LogUtil.log("order cancel:" + uuid);
	}

	@Override
	public void confirm(Long uuid) throws ParticipantException {
		if (!payment.isConfirmed(uuid) || !sale.isConfirmed(uuid))
 			throw new ParticipantException("fuck");
		LogUtil.log("order confirm:" + uuid);
	}

	@Override
	public void expired(Long uuid) throws ParticipantException {
		LogUtil.log("order expired:" + uuid);
	}
}
