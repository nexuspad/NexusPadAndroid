package com.nexuspad.contacts.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntryFragment;

import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: Edmond
 */
@FragmentName(ContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactFragment extends EntryFragment<Contact> {
    public static final String TAG = "ContactFragment";

    public static ContactFragment of(Contact contact, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, contact);
        bundle.putParcelable(KEY_FOLDER, folder);

        final ContactFragment fragment = new ContactFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mTitleV;
    private TextView mFirstNameV;
    private TextView mMiddleNameV;
    private TextView mLastNameV;
    private TextView mBussinessNameV;
    private TextView mWebAddressV;
    private TextView mTagsV;
    private TextView mNoteV;

    private View mPhoneHeaderV;
    private View mEmailHeaderV;
    private TextView mWebAddressHeaderV;
    private TextView mTagsHeaderV;
    private TextView mNoteHeaderV;

    private ViewGroup mPhoneFrameV;
    private ViewGroup mEmailFrameV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTitleV = findView(view, android.R.id.title);
        mFirstNameV = findView(view, R.id.lbl_first_name);
        mMiddleNameV = findView(view, R.id.lbl_middle_name);
        mLastNameV = findView(view, R.id.lbl_last_name);
        mBussinessNameV = findView(view, R.id.lbl_bussiness_name);
        mWebAddressV = findView(view, R.id.lbl_web_address);
        mTagsV = findView(view, R.id.lbl_tags);
        mNoteV = findView(view, R.id.lbl_note);

        mPhoneHeaderV = findView(view, R.id.lbl_phones);
        mEmailHeaderV = findView(view, R.id.lbl_emails);
        mWebAddressHeaderV = findView(view, R.id.lbl_web_address_frame);
        mTagsHeaderV = findView(view, R.id.lbl_tags_frame);
        mNoteHeaderV = findView(view, R.id.lbl_note_frame);

        mPhoneFrameV = findView(view, R.id.phones_frame);
        mEmailFrameV = findView(view, R.id.emails_frame);

        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        final Contact contact = getDetailEntryIfExist();
        if (contact != null) {
            mTitleV.setText(contact.getTitle());
            mFirstNameV.setText(contact.getFirstName());
            mMiddleNameV.setText(contact.getMiddleName());
            mLastNameV.setText(contact.getLastName());
            mBussinessNameV.setText(contact.getBusinessName());
            mWebAddressV.setText(contact.getWebAddress());
            mTagsV.setText(contact.getTags());
            mNoteV.setText(contact.getNote());

            updatePhones(contact);
            updateEmails(contact);
            updateVisibility(contact);
        }
    }

    private void updatePhones(Contact contact) {
        mPhoneFrameV.removeAllViews();
        final List<Phone> phones = contact.getPhones();
        if (!phones.isEmpty()) {
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (Phone phone : phones) {
                addBasicItemView(phone, mPhoneFrameV, inflater);
            }
        }
    }

    private void updateEmails(Contact contact) {
        mEmailFrameV.removeAllViews();
        final List<Email> emails = contact.getEmails();
        if (!emails.isEmpty()) {
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (Email email : emails) {
                addBasicItemView(email, mEmailFrameV, inflater);
            }
        }
    }

    private void addBasicItemView(BasicItem item, ViewGroup target, LayoutInflater inflater) {
        final ViewGroup frame = (ViewGroup) inflater.inflate(R.layout.layout_selectable_frame, null);
        final View view = inflater.inflate(R.layout.list_item_icon, null);

        final TextView text = findView(view, android.R.id.text1);
        final View menu = findView(view, R.id.menu);

        text.setLinksClickable(false);
        text.setAutoLinkMask(Linkify.ALL);
        text.setText(item.getValue());
        menu.setVisibility(View.GONE);

        frame.setOnClickListener(onCreateOnClickListener(item));

        frame.addView(view);
        target.addView(frame);
    }

    private View.OnClickListener onCreateOnClickListener(BasicItem item) {
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
                return  new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        App.sendEmail(value, getActivity());
                    }
                };
            default:
                throw new AssertionError("unexpected type: " + item.getType());
        }
    }

    private void updateVisibility(Contact contact) {
        final int phonesFlag = contact.getPhones().isEmpty() ? View.GONE : View.VISIBLE;
        final int emailsFlag = contact.getEmails().isEmpty() ? View.GONE : View.VISIBLE;
        final int webAddressFlag = TextUtils.isEmpty(contact.getWebAddress()) ? View.GONE : View.VISIBLE;
        final int tagsFlag = TextUtils.isEmpty(contact.getTags()) ? View.GONE : View.VISIBLE;
        final int noteFlag = TextUtils.isEmpty(contact.getNote()) ? View.GONE : View.VISIBLE;

        mPhoneHeaderV.setVisibility(phonesFlag);
        mEmailHeaderV.setVisibility(emailsFlag);

        mWebAddressHeaderV.setVisibility(webAddressFlag);
        mTagsHeaderV.setVisibility(tagsFlag);
        mNoteHeaderV.setVisibility(noteFlag);

        mWebAddressV.setVisibility(webAddressFlag);
        mTagsV.setVisibility(tagsFlag);
        mNoteV.setVisibility(noteFlag);
    }

    @Override
    protected void onEntryUpdated(Contact entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Contact entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }
}
