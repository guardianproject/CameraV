package org.witness.iwitness.app.screens.menus;

import java.util.List;

import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.actions.ContextMenuAction;
import org.witness.iwitness.utils.adapters.ContextMenuSimpleAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class MediaActionMenu extends AlertDialog.Builder implements OnClickListener  {
	public Dialog alert;
	ListView contextMenuOptionsHolder;
	
	View layout;
	
	private final static String LOG = App.LOG;
	
	public MediaActionMenu(Activity a, List<ContextMenuAction> actions){
		super(a);
		
		layout = LayoutInflater.from(a).inflate(R.layout.extras_context_menu, null);
		contextMenuOptionsHolder = (ListView) layout.findViewById(R.id.context_menu_options_holder);
		contextMenuOptionsHolder.setAdapter(new ContextMenuSimpleAdapter(actions, a));
		layout.setOnClickListener(this);
		
		alert = create();
		setView(layout);
	}
	
	public void Show() {
		alert = this.show();
	}
	
	public void cancel() {
		alert.cancel();
	}

	@Override
	public void onClick(View v) {
		Log.d(LOG, "hey i clicked on myself");
		
	}

}
