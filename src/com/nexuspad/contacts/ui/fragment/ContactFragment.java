package com.nexuspad.contacts.ui.fragment;

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
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.contacts.ui.activity.NewContactActivity;
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

    private LayoutInflater mInflater;

    private TextView mTitleV;
    private TextView mFirstNameV;
    private TextView mMiddleNameV;
    private TextView mLastNameV;
    private TextView mBussinessNameV;
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
                NewContactActivity.startWith(getActivity(), getEntry(), getFolder());
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
        mTitleV = findView(view, android.R.id.title);
        mFirstNameV = findView(view, R.id.lbl_first_name);
        mMiddleNameV = findView(view, R.id.lbl_middle_name);
        mLastNameV = findView(view, R.id.lbl_last_name);
        mBussinessNameV = findView(view, R.id.lbl_bussiness_name);
        mWebAddressV = findView(view, R.id.lbl_web_address);
        mTagsV = findView(view, R.id.lbl_tags);
        mNoteV = findView(view, R.id.lbl_note);
        mAddressV = findView(view, R.id.lbl_address);

        mPhoneHeaderV = findView(view, R.id.lbl_phones);
        mEmailHeaderV = findView(view, R.id.lbl_emails);
        mWebAddressHeaderV = findView(view, R.id.lbl_web_address_frame);
        mTagsHeaderV = findView(view, R.id.lbl_tags_frame);
        mNoteHeaderV = findView(view, R.id.lbl_note_frame);
        mAddressHeaderV = findView(view, R.id.lbl_address_frame);

        mPhoneFrameV = findView(view, R.id.phones_frame);
        mEmailFrameV = findView(view, R.id.emails_frame);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        final Contact contact = getEntry();
        if (contact != null) {
            mTitleV.setText(contact.getTitle());
            mFirstNameV.setText(contact.getFirstName());
            mMiddleNameV.setText(contact.getMiddleName());
            mLastNameV.setText(contact.getLastName());
            mBussinessNameV.setText(contact.getBusinessName());
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

    private void updatePhones(Contact contact) {
        mPhoneFrameV.removeAllViews();
        final List<Phone> phones = contact.getPhones();
        if (!phones.isEmpty()) {
            for (Phone phone : phones) {
                addBasicItemView(phone, mPhoneFrameV);
            }
        }
    }

    private void updateEmails(Contact contact) {
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

        final TextView text = findView(view, android.R.id.text1);
        final View icon = findView(view, android.R.id.icon);
        final View menu = findView(view, R.id.menu);

        text.setLinksClickable(false);
        text.setAutoLinkMask(Linkify.ALL);
        text.setText(item.getValue());
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

    private void updateVisibility(Contact contact) {
        final int firstNameFlag = TextUtils.isEmpty(contact.getFirstName()) ? View.GONE : View.VISIBLE;
        final int middleNameFlag = TextUtils.isEmpty(contact.getMiddleName()) ? View.GONE : View.VISIBLE;
        final int lastNameFlag = TextUtils.isEmpty(contact.getLastName()) ? View.GONE : View.VISIBLE;
        final int businessNameFlag = TextUtils.isEmpty(contact.getBusinessName()) ? View.GONE : View.VISIBLE;

        final int phonesFlag = contact.getPhones().isEmpty() ? View.GONE : View.VISIBLE;
        final int emailsFlag = contact.getEmails().isEmpty() ? View.GONE : View.VISIBLE;
        final int webAddressFlag = TextUtils.isEmpty(contact.getWebAddress()) ? View.GONE : View.VISIBLE;
        final int tagsFlag = TextUtils.isEmpty(contact.getTags()) ? View.GONE : View.VISIBLE;
        final int noteFlag = TextUtils.isEmpty(contact.getNote()) ? View.GONE : View.VISIBLE;
        final int addressFlag = TextUtils.isEmpty(contact.getLocation().getFullAddress()) ? View.GONE : View.VISIBLE;

        mFirstNameV.setVisibility(firstNameFlag);
        mMiddleNameV.setVisibility(middleNameFlag);
        mLastNameV.setVisibility(lastNameFlag);
        mBussinessNameV.setVisibility(businessNameFlag);

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
