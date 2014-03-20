package com.middleware.pubsubclient;


import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.NodeType;
import org.jivesoftware.smackx.pubsub.PublishModel;

public class NodeConfig {
	
	public static Form setNodeConfig()
	{
	ConfigureForm form = new ConfigureForm(FormType.submit);
	form.setPersistentItems(true);
	form.setDeliverPayloads(true);
	form.setAccessModel(AccessModel.open);
	form.setPublishModel(PublishModel.open);
	form.setNodeType(NodeType.leaf);
	
	return form;
	}
	
}
