package com.cz.jobcoin.mixer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cz.jobcoin.mixer.data.model.MixRequest;
import com.cz.jobcoin.mixer.data.model.MixResponse;
import com.cz.jobcoin.mixer.services.MixerService;

@RestController
public class MixerController {
	@Autowired
	MixerService mixerService;
	@RequestMapping(value = "/mixer/api/mix", method = RequestMethod.POST)
	  public MixResponse processMixRequest(@RequestBody MixRequest mixRequest)  {
		MixResponse response = new MixResponse();
		response=  mixerService.processMixRequest(mixRequest);
		return response;
	  }
}
