package org.witness.informacam.app.screens.menus;

import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.actions.ContextMenuAction;
import org.witness.informacam.app.utils.adapters.ContextMenuSimpleAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class MediaActionMenu extends AlertDialog.Builder  {
	public Dialog alert;
	ListView contextMenuOptionsHolder;
	
	View layout;
		
	public MediaActionMenu(Activity a, List<ContextMenuAction> actions){
		super(a);
		
		layout = LayoutInflater.from(a).inflate(R.layout.extras_context_menu, null);
		contextMenuOptionsHolder = (ListView) layout.findViewById(R.id.context_menu_options_holder);
		contextMenuOptionsHolder.setAdapter(new ContextMenuSimpleAdapter(actions, a));
		
		alert = create();
		setView(layout);
	}
	
	public void Show() {
		alert = this.show();
	}
	
	public void cancel() {
		alert.cancel();
	}
}
