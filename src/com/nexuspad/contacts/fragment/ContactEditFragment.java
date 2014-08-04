package com.nexuspad.contacts.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.contacts.activity.LocationEditActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * Author: edmond
 */
@FragmentName(ContactEditFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactEditFragment extends EntryEditFragment<NPPerson> {
    public static final String TAG = "ContactEditFragment";

    public static ContactEditFragment of(NPPerson contact, NPFolder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, contact);
        bundle.putParcelable(KEY_FOLDER, folder);

        final ContactEditFragment fragment = new ContactEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private static final int LOCATION_EDIT_REQUEST = 2;

    private LayoutInflater mInflater;

    private TextView mFolderV;
    private EditText mTitleV;
    private TextView mFirstNameV;
    private TextView mMiddleNameV;
    private TextView mLastNameV;
    private TextView mBizNameV;
    private TextView mWebAddressV;
    private TextView mTagsV;
    private TextView mNoteV;
    private TextView mAddressV;

    private ViewGroup mPhoneFrameV;
    private ViewGroup mEmailFrameV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_EDIT_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    final Location location = data.getParcelableExtra(LocationEditActivity.KEY_LOCATION);
                    updateAddressView(location);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_edit_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFolderV = (TextView)view.findViewById(R.id.lbl_folder);
        mTitleV = (EditText)view.findViewById(android.R.id.title);
        mFirstNameV = (EditText)view.findViewById(R.id.txt_first_name);
        mMiddleNameV = (EditText)view.findViewById(R.id.txt_middle_name);
        mLastNameV = (EditText)view.findViewById(R.id.txt_last_name);
        mBizNameV = (EditText)view.findViewById(R.id.txt_bussiness_name);
        mWebAddressV = (EditText)view.findViewById(R.id.txt_web_address);
        mTagsV = (EditText)view.findViewById(R.id.txt_tags);
        mNoteV = (EditText)view.findViewById(R.id.journal_text);
        mAddressV = (TextView)view.findViewById(R.id.txt_address);

        mPhoneFrameV = (ViewGroup)view.findViewById(R.id.phones_frame);
        mEmailFrameV = (ViewGroup)view.findViewById(R.id.emails_frame);

        mAddressV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = LocationEditActivity.of(getActivity(), (Location) mAddressV.getTag());
                startActivityForResult(intent, LOCATION_EDIT_REQUEST);
            }
        });

        installFolderSelectorListener(mFolderV);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onFolderUpdated(NPFolder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    private void updateFolderView() {
        mFolderV.setText(getFolder().getFolderName());
    }

    @Override
    protected void updateUI() {
        updateFolderView();

        final NPPerson contact = getEntry();
        if (contact != null) {
            mTitleV.setText(contact.getTitle());
            mFirstNameV.setText(contact.getFirstName());
            mMiddleNameV.setText(contact.getMiddleName());
            mLastNameV.setText(contact.getLastName());
            mBizNameV.setText(contact.getBusinessName());
            mWebAddressV.setText(contact.getWebAddress());
            mTagsV.setText(contact.getTags());
            mNoteV.setText(contact.getNote());

            updateAddressView(contact.getLocation());
            updatePhones(contact);
            updateEmails(contact);
        }

        addNPItemViewIfNeeded(null, NPItem.ItemType.PHONE);
        addNPItemViewIfNeeded(null, NPItem.ItemType.EMAIL);
    }

    private void updateAddressView(Location location) {
        mAddressV.setText(location.getFullAddress());
        mAddressV.setTag(location);
    }

    private void updatePhones(NPPerson contact) {
        for (Phone phone : contact.getPhones()) {
            addPhoneView(phone);
        }
    }

    private void updateEmails(NPPerson contact) {
        for (Email email : contact.getEmails()) {
            addEmailView(email);
        }
    }

    /**
     * Add an email view to the email frame
     *
     * @param email used to set the text field if not null
     */
    private void addEmailView(final Email email) {
        addNPItemView(email, NPItem.ItemType.EMAIL);
    }

    /**
     * Add a phone view to the phone frame
     *
     * @param phone used to set the text field if not null
     */
    private void addPhoneView(final Phone phone) {
        addNPItemView(phone, NPItem.ItemType.PHONE);
    }

    private void addNPItemViewIfNeeded(NPItem item, final NPItem.ItemType type) {
        final ViewGroup frame = getParentFor(type);
        final int childCount = frame.getChildCount();
        if (childCount <= 0) {
            addNPItemView(item, type);
        } else {
            final View parent = frame.getChildAt(childCount - 1);
            final EditText editText = (EditText)parent.findViewById(android.R.id.edit);
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                addNPItemView(item, type);
            }
        }
    }

    private void addNPItemView(final NPItem item, final NPItem.ItemType type) {
        final ViewGroup parent = getParentFor(type);
        final int hintId = getHintIdFor(type);
        final View view = mInflater.inflate(R.layout.list_item_edittext_btn, parent, false);

        final EditText editText = (EditText)view.findViewById(android.R.id.edit);
        final ImageView closeButton = (ImageView)view.findViewById(android.R.id.button1);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(view);
            }
        });
        if (item != null) {
            editText.setText(item.getValue());
        }
        editText.setHint(hintId);
        editText.setInputType(getInputTypeFor(type));
        editText.addTextChangedListener(new EmptyTextWatcher() {
            @Override
            protected void onTextNotEmpty(CharSequence sequence) {
                addNPItemViewIfNeeded(item, type);
            }
        });

        parent.addView(view);
    }

    private ViewGroup getParentFor(NPItem.ItemType type) {
        switch (type) {
            case PHONE:
                return mPhoneFrameV;
            case EMAIL:
                return mEmailFrameV;
            default:
                throw new AssertionError("unexpected type: " + type);
        }
    }

    private int getHintIdFor(NPItem.ItemType type) {
        switch (type) {
            case PHONE:
                return R.string.phone;
            case EMAIL:
                return R.string.email;
            default:
                throw new AssertionError("unexpected type: " + type);
        }
    }

    private int getInputTypeFor(NPItem.ItemType type) {
        switch (type) {
            case PHONE:
                return InputType.TYPE_CLASS_PHONE;
            case EMAIL:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            default:
                throw new AssertionError("unexpected type: " + type);
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return true;
    }

    @Override
    public NPPerson getEntryFromEditor() {
        final NPPerson entry = getEntry();
        final NPPerson contact = entry == null ? new NPPerson(getFolder()) : new NPPerson(entry);

        Location location = (Location) mAddressV.getTag();
        if (location == null) {
            location = new Location();
        }
        contact.setLocation(location);
        contact.setTitle(mTitleV.getText().toString());
        contact.setFirstName(mFirstNameV.getText().toString());
        contact.setMiddleName(mMiddleNameV.getText().toString());
        contact.setLastName(mLastNameV.getText().toString());
        contact.setBusinessName(mBizNameV.getText().toString());
        contact.setWebAddress(mWebAddressV.getText().toString());
        contact.setTags(mTagsV.getText().toString());
        contact.setNote(mNoteV.getText().toString());

        putPhonesInto(contact);
        putEmailsInto(contact);

        setEntry(contact);
        return contact;
    }

    private void putPhonesInto(NPPerson contact) {
        for (int i = 0, count = mPhoneFrameV.getChildCount(); i < count; ++i) {
            final View view = mPhoneFrameV.getChildAt(i);
            final EditText text = (EditText)view.findViewById(android.R.id.edit);
            final String phone = text.getText().toString();
            if (!TextUtils.isEmpty(phone)) {
                contact.addPhone(new Phone(phone));
            }
        }
    }

    private void putEmailsInto(NPPerson contact) {
        for (int i = 0, count = mEmailFrameV.getChildCount(); i < count; ++i) {
            final View view = mEmailFrameV.getChildAt(i);
            final EditText text = (EditText)view.findViewById(android.R.id.edit);
            final String email = text.getText().toString();
            if (!TextUtils.isEmpty(email)) {
                contact.addEmail(new Email(email));
            }
        }
    }

    private static abstract class EmptyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                onTextNotEmpty(s);
            }
        }

        protected abstract void onTextNotEmpty(CharSequence sequence);
    }
}
