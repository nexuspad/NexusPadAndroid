package com.nexuspad.contacts.ui.fragment;

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
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.activity.NewLocationActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.NewEntryFragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: edmond
 */
@FragmentName(NewContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class NewContactFragment extends NewEntryFragment<Contact> {
    public static final String TAG = "NewContactFragment";

    public static NewContactFragment of(Contact contact, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, contact);
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewContactFragment fragment = new NewContactFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    // 1 is used by REQ_FOLDER (NewEntryFragment)
    private static final int REQ_LOCATION = 2;

    private LayoutInflater mInflater;

    private EditText mTitleV;
    private TextView mFirstNameV;
    private TextView mMiddleNameV;
    private TextView mLastNameV;
    private TextView mBussinessNameV;
    private TextView mWebAddressV;
    private TextView mTagsV;
    private TextView mNoteV;
    // setTag()/getTag() is used for storing the Location object
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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_LOCATION:
                if (resultCode != Activity.RESULT_OK) break;
                final Location location = data.getParcelableExtra(NewLocationActivity.KEY_LOCATION);
                updateAddressView(location);
                break;
            default:
                throw new AssertionError("unexpected requestCode: " + requestCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTitleV = findView(view, android.R.id.title);
        mFirstNameV = findView(view, R.id.txt_first_name);
        mMiddleNameV = findView(view, R.id.txt_middle_name);
        mLastNameV = findView(view, R.id.txt_last_name);
        mBussinessNameV = findView(view, R.id.txt_bussiness_name);
        mWebAddressV = findView(view, R.id.txt_web_address);
        mTagsV = findView(view, R.id.txt_tags);
        mNoteV = findView(view, R.id.txt_note);
        mAddressV = findView(view, R.id.txt_address);

        mPhoneFrameV = findView(view, R.id.phones_frame);
        mEmailFrameV = findView(view, R.id.emails_frame);

        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
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
            updateAddressView(contact.getLocation());
            mAddressV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = NewLocationActivity.of(getActivity(), (Location) mAddressV.getTag());
                    startActivityForResult(intent, REQ_LOCATION);
                }
            });

            updatePhones(contact);
            updateEmails(contact);
        }
    }

    private void updateAddressView(Location location) {
        mAddressV.setText(location.getFullAddress());
        mAddressV.setTag(location);
    }

    private void updatePhones(Contact contact) {
        for (Phone phone : contact.getPhones()) {
            addPhoneView(phone);
        }
        addPhoneView(null);
    }

    private void updateEmails(Contact contact) {
        for (Email email : contact.getEmails()) {
            addEmailView(email);
        }
        addEmailView(null);
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

    private void addNPItemViewIfNeeded(NPItem NPItem, final NPItem.ItemType type) {
        final ViewGroup frame = getParentFor(type);
        final int childCount = frame.getChildCount();
        if (childCount <= 0) return;
        final View parent = frame.getChildAt(childCount - 1);
        final EditText editText = findView(parent, android.R.id.edit);
        if (!TextUtils.isEmpty(editText.getText().toString())) {
            addNPItemView(NPItem, type);
        }
    }

    private void addNPItemView(final NPItem NPItem, final NPItem.ItemType type) {
        final ViewGroup parent = getParentFor(type);
        final int hintId = getHintIdFor(type);
        final View view = mInflater.inflate(R.layout.list_item_edittext_btn, parent, false);

        final EditText editText = findView(view, android.R.id.edit);
        final View closeButton = findView(view, android.R.id.button1);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(view);
            }
        });
        if (NPItem != null) {
            editText.setText(NPItem.getValue());
        }
        editText.setHint(hintId);
        editText.setInputType(getInputTypeFor(type));
        editText.addTextChangedListener(new EmptyTextWatcher() {
            @Override
            protected void onTextNotEmpty(CharSequence sequence) {
                addNPItemViewIfNeeded(NPItem, type);
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
        return ViewUtils.isAllTextNotEmpty(R.string.err_empty_field, mTitleV);
    }

    @Override
    public Contact getEditedEntry() {
        final Contact entry = getDetailEntryIfExist();
        final Contact contact = entry == null ? new Contact(getFolder()) : new Contact(entry);

        contact.setLocation((Location) mAddressV.getTag());
        contact.setTitle(mTitleV.getText().toString());
        contact.setFirstName(mFirstNameV.getText().toString());
        contact.setMiddleName(mMiddleNameV.getText().toString());
        contact.setLastName(mLastNameV.getText().toString());
        contact.setBusinessName(mBussinessNameV.getText().toString());
        contact.setWebAddress(mWebAddressV.getText().toString());
        contact.setTags(mTagsV.getText().toString());
        contact.setNote(mNoteV.getText().toString());

        putPhonesInto(contact);
        putEmailsInto(contact);

        setDetailEntry(contact);
        return contact;
    }

    private void putPhonesInto(Contact contact) {
        for (int i = 0, count = mPhoneFrameV.getChildCount(); i < count; ++i) {
            final View view = mPhoneFrameV.getChildAt(i);
            final EditText text = findView(view, android.R.id.edit);
            final String phone = text.getText().toString();
            if (!TextUtils.isEmpty(phone)) {
                contact.addPhone(new Phone(phone));
            }
        }
    }

    private void putEmailsInto(Contact contact) {
        for (int i = 0, count = mEmailFrameV.getChildCount(); i < count; ++i) {
            final View view = mEmailFrameV.getChildAt(i);
            final EditText text = findView(view, android.R.id.edit);
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
