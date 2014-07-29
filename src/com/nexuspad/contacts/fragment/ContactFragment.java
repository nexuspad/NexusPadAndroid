package com.nexuspad.contacts.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.nexuspad.common.annotation.FragmentName;
import com.google.common.base.Strings;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.contacts.activity.UpdateContactActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.common.fragment.EntryFragment;

import java.util.List;

/**
 * Author: Edmond
 */
@FragmentName(ContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactFragment extends EntryFragment<NPPerson> {
	public static final String TAG = "ContactFragment";

	public static ContactFragment of(NPPerson contact, NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_ENTRY, contact);
		bundle.putParcelable(KEY_FOLDER, folder);

		final ContactFragment fragment = new ContactFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	private LayoutInflater mInflater;

	private TextView mFullnameView;

	private TextView mBizNameHeaderView;
	private TextView mBizNameView;

	private TextView mWebAddressV;
	private TextView mTagsV;
	private TextView mNoteV;
	private TextView mAddressV;

	private View mPhoneHeaderV;
	private View mEmailHeaderV;
	private TextView mWebAddressHeaderV;
	private TextView mTagsHeaderV;
	private TextView mNoteHeaderV;
	private TextView mAddressHeaderV;

	private ViewGroup mPhoneFrameV;
	private ViewGroup mEmailFrameV;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mInflater = LayoutInflater.from(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.contact_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.edit:
				UpdateContactActivity.startWithContact(getActivity(), getFolder(), getEntry());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contact_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mFullnameView = (TextView)view.findViewById(R.id.lbl_full_name);

		mBizNameHeaderView = (TextView)view.findViewById(R.id.lbl_business_name_title);
		mBizNameView = (TextView)view.findViewById(R.id.lbl_bussiness_name);

		mWebAddressV = (TextView)view.findViewById(R.id.lbl_web_address);
		mTagsV = (TextView)view.findViewById(R.id.lbl_tags);
		mNoteV = (TextView)view.findViewById(R.id.lbl_note);
		mAddressV = (TextView)view.findViewById(R.id.lbl_address);

		mPhoneHeaderV = view.findViewById(R.id.lbl_phones);
		mEmailHeaderV = view.findViewById(R.id.lbl_emails);
		mWebAddressHeaderV = (TextView)view.findViewById(R.id.lbl_web_address_frame);
		mTagsHeaderV = (TextView)view.findViewById(R.id.lbl_tags_title);
		mNoteHeaderV = (TextView)view.findViewById(R.id.lbl_note_frame);
		mAddressHeaderV = (TextView)view.findViewById(R.id.lbl_address_frame);

		mPhoneFrameV = (ViewGroup)view.findViewById(R.id.phones_frame);
		mEmailFrameV = (ViewGroup)view.findViewById(R.id.emails_frame);

		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	protected void updateUI() {
		final NPPerson contact = getEntry();
		if (contact != null) {
			mFullnameView.setText(contact.getFullName());
			mBizNameView.setText(contact.getBusinessName());
			mWebAddressV.setText(contact.getWebAddress());
			mTagsV.setText(contact.getTags());
			mNoteV.setText(contact.getNote());
			mAddressV.setText(contact.getLocation().getFullAddress());
			mAddressV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String fullAddress = contact.getLocation().getFullAddress();
					final Uri uri = Uri.parse("geo:0,0?q=" + fullAddress);
					final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(getActivity(), R.string.err_no_map, Toast.LENGTH_SHORT).show();
					}
				}
			});

			updatePhones(contact);
			updateEmails(contact);
			updateVisibility(contact);
		}
	}

	private void updatePhones(NPPerson contact) {
		mPhoneFrameV.removeAllViews();
		final List<Phone> phones = contact.getPhones();
		if (!phones.isEmpty()) {
			for (Phone phone : phones) {
				addBasicItemView(phone, mPhoneFrameV);
			}
		}
	}

	private void updateEmails(NPPerson contact) {
		mEmailFrameV.removeAllViews();
		final List<Email> emails = contact.getEmails();
		if (!emails.isEmpty()) {
			for (Email email : emails) {
				addBasicItemView(email, mEmailFrameV);
			}
		}
	}

	private void addBasicItemView(NPItem item, ViewGroup target) {
		final ViewGroup frame = (ViewGroup) mInflater.inflate(R.layout.layout_selectable_frame, null);
		final ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.list_item_icon, frame, false);

		final TextView text = (TextView)view.findViewById(android.R.id.text1);
		final View icon = view.findViewById(android.R.id.icon);
		final View menu = view.findViewById(R.id.menu);

		text.setLinksClickable(false);
		text.setAutoLinkMask(Linkify.ALL);

		if (!Strings.isNullOrEmpty(item.getLabel()) && "phone".equalsIgnoreCase(item.getLabel()) && !"email".equalsIgnoreCase(item.getLabel())) {
			text.setText(item.getFormattedValue() + " (" + item.getLabel() + ")");
		} else {
			text.setText(item.getFormattedValue());
		}

		view.removeView(icon);
		view.removeView(menu);

		frame.setOnClickListener(onCreateOnClickListener(item));

		frame.addView(view);
		target.addView(frame);
	}

	private View.OnClickListener onCreateOnClickListener(NPItem item) {
		final String value = item.getValue();
		switch (item.getType()) {
			case PHONE:
				return new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final Intent phone = new Intent(Intent.ACTION_DIAL);
						phone.setData(Uri.parse("tel:" + value));
						startActivity(phone);
					}
				};
			case EMAIL:
				return new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						App.sendEmail(value, getActivity());
					}
				};
			default:
				throw new AssertionError("unexpected type: " + item.getType());
		}
	}

	private void updateVisibility(NPPerson contact) {
		String fullName = contact.getFullName();
		if (fullName == null || fullName.length() == 0 || contact.getTitle().equals(fullName)) {
			mFullnameView.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(contact.getBusinessName())) {
			mBizNameHeaderView.setVisibility(View.GONE);
			mBizNameView.setVisibility(View.GONE);
		} else {
			mBizNameHeaderView.setVisibility(View.VISIBLE);
			mBizNameView.setVisibility(View.VISIBLE);
		}

		final int phonesFlag = contact.getPhones().isEmpty() ? View.GONE : View.VISIBLE;
		final int emailsFlag = contact.getEmails().isEmpty() ? View.GONE : View.VISIBLE;
		final int webAddressFlag = TextUtils.isEmpty(contact.getWebAddress()) ? View.GONE : View.VISIBLE;
		final int tagsFlag = TextUtils.isEmpty(contact.getTags()) ? View.GONE : View.VISIBLE;
		final int noteFlag = TextUtils.isEmpty(contact.getNote()) ? View.GONE : View.VISIBLE;
		final int addressFlag = TextUtils.isEmpty(contact.getLocation().getFullAddress()) ? View.GONE : View.VISIBLE;

		mPhoneHeaderV.setVisibility(phonesFlag);
		mEmailHeaderV.setVisibility(emailsFlag);

		mWebAddressHeaderV.setVisibility(webAddressFlag);
		mTagsHeaderV.setVisibility(tagsFlag);
		mNoteHeaderV.setVisibility(noteFlag);
		mAddressHeaderV.setVisibility(addressFlag);

		mWebAddressV.setVisibility(webAddressFlag);
		mTagsV.setVisibility(tagsFlag);
		mNoteV.setVisibility(noteFlag);
		mAddressV.setVisibility(addressFlag);
	}
}
