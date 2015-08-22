package org.witness.informacam.models.organizations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.ui.popups.YesNoPopup;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class IInstalledOrganizations extends Model {
	public List<IOrganization> organizations = new ArrayList<IOrganization>();
	
	public List<IOrganization> listOrganizations() {
		return organizations;
	}
	
	public IOrganization getByName(final String organizationName) {
		Collection<IOrganization> organizations_ = Collections2.filter(organizations, new Predicate<IOrganization>() {
			@Override
			public boolean apply(IOrganization o) {
				return o.organizationName.equals(organizationName);
			}
		});
		
		try {
			return organizations_.iterator().next();
		} catch(NullPointerException e) {
			return null;
		} catch(NoSuchElementException e) {
			return null;
		}
	}
	
	@SuppressLint("DefaultLocale")
	public IOrganization getByFingerprint(final String fingerprint) {
		Collection<IOrganization> organizations_ = Collections2.filter(organizations, new Predicate<IOrganization>() {
			@Override
			public boolean apply(IOrganization o) {
				return o.organizationFingerprint.toLowerCase().equals(fingerprint.toLowerCase());
			}
		});
		
		try {
			return organizations_.iterator().next();
		} catch(NullPointerException e) {
			return null;
		} catch(NoSuchElementException e) {
			return null;
		}
	}
	
	
	public void save() {
		InformaCam.getInstance().saveState(this);
	}

	public void addOrganization(final IOrganization organization, Context a) {
		final IOrganization possibleDuplicate = getByFingerprint(organization.organizationFingerprint);
		
		if(possibleDuplicate == null) {
			organizations.add(organization);
			save();
		} else {
			boolean isActuallyDifferent = false;
			StringBuffer sb = new StringBuffer();
			
			ArrayList<View> infoViews = new ArrayList<View>();
			
			sb.append(a.getString(R.string.an_ictd_for_x_already, organization.organizationName));
			sb.append("\n\n" + a.getString(R.string.heres_whats_changed) + "\n");
			
			TextView warning = new TextView(a);
			warning.setText(sb.toString());
			infoViews.add(warning);
			
			Logger.d(LOG, "OLD REPO:\n" + possibleDuplicate.asJson().toString());
			Logger.d(LOG, "NEW REPO:\n" + organization.asJson().toString());
			
			if(!possibleDuplicate.organizationDetails.equals(organization.organizationDetails)) {
				TextView orgDetails = new TextView(a);
				orgDetails.setText(Models.IOrganization.ORGANIZATION_DETAILS + ":");
				orgDetails.setTypeface(null, Typeface.BOLD);
				
				TextView orgDetails_ = new TextView(a);
				sb = new StringBuffer();
				sb.append(a.getString(R.string.old_version) + ": " + possibleDuplicate.organizationDetails + "\n");
				sb.append(a.getString(R.string.new_version) + ": " + organization.organizationDetails + "\n");
				orgDetails_.setText(sb.toString());
				
				infoViews.add(orgDetails);
				infoViews.add(orgDetails_);
				
				if(!isActuallyDifferent) {
					isActuallyDifferent = true;
				}
			}
			
			if(!possibleDuplicate.organizationName.equals(organization.organizationName)) {
				TextView orgDetails = new TextView(a);
				orgDetails.setText(Models.IOrganization.ORGANIZATION_NAME + ":");
				orgDetails.setTypeface(null, Typeface.BOLD);
				
				TextView orgDetails_ = new TextView(a);
				sb = new StringBuffer();
				sb.append(a.getString(R.string.old_version) + ": " + possibleDuplicate.organizationName + "\n");
				sb.append(a.getString(R.string.new_version) + ": " + organization.organizationName + "\n");
				orgDetails_.setText(sb.toString());
				
				infoViews.add(orgDetails);
				infoViews.add(orgDetails_);
				
				if(!isActuallyDifferent) {
					isActuallyDifferent = true;
				}
			}
			
			if(possibleDuplicate.repositories != organization.repositories) {
				TextView orgDetails = new TextView(a);
				orgDetails.setText(Models.IOrganization.REPOSITORIES + ":");
				orgDetails.setTypeface(null, Typeface.BOLD);
				
				TextView orgDetails_ = new TextView(a);
				sb = new StringBuffer();
				sb.append(a.getString(R.string.old_version) + ":\n");
				for(IRepository r : possibleDuplicate.repositories) {
					sb.append(r.source + " : " + r.asset_root + "/" + r.asset_id + "\n");
				}
				sb.append("\n");
				
				sb.append(a.getString(R.string.new_version) + ":\n");
				for(IRepository r : organization.repositories) {
					sb.append(r.source + " : " + r.asset_root + "/" + r.asset_id + "\n");
				}
				
				orgDetails_.setText(sb.toString());
				
				infoViews.add(orgDetails);
				infoViews.add(orgDetails_);
				
				if(!isActuallyDifferent) {
					isActuallyDifferent = true;
				}
			}
			
			if(possibleDuplicate.forms != organization.forms) {
				TextView orgDetails = new TextView(a);
				orgDetails.setText(Models.IOrganization.FORMS + ":");
				orgDetails.setTypeface(null, Typeface.BOLD);
				
				TextView orgDetails_ = new TextView(a);
				sb = new StringBuffer();
				sb.append(a.getString(R.string.old_version) + ":\n");
				for(IForm f : possibleDuplicate.forms) {
					sb.append(f.title + "\n");
				}
				sb.append("\n");
				
				sb.append(a.getString(R.string.new_version) + ":\n");
				for(IForm f : organization.forms) {
					sb.append(f.title + "\n");
				}
				
				orgDetails_.setText(sb.toString());
				
				infoViews.add(orgDetails);
				infoViews.add(orgDetails_);
				
				if(!isActuallyDifferent) {
					isActuallyDifferent = true;
				}
			}
			
			if(!isActuallyDifferent) {
				TextView orgDetails = new TextView(a);
				orgDetails.setText(a.getString(R.string.this_ictd_has_not_changed));
				
				infoViews.add(orgDetails);
			}
			/*
			@SuppressWarnings("unused")
			YesNoPopup updateICTDPopup = new YesNoPopup(a, infoViews) {
				@Override
				public void onClick(View v) {
					if(v == ok) {
						try
						{
							possibleDuplicate.inflate(organization);
							save();
						}
						catch (Exception e)
						{
							Logger.e(LOG, e);
						}
					}
					
					cancel();
				}
			};*/
			
			
		}
		
		Logger.d(LOG, String.format("Installed Organizatins: %d", organizations.size()));

	}
	
}
