package com.nexuspad.service.datamodel;

/**
 * Created by pzq8h1 on 9/2/2014.
 */
public class NPDateRange {
	private String mStartYmd;
	private String mEndYmd;

	public NPDateRange(String startYmd, String endYmd) {
		mStartYmd = startYmd;
		mEndYmd = endYmd;
	}

	public NPDateRange(NPDateRange aDateRange) {
		mStartYmd = aDateRange.getStartYmd();
		mEndYmd = aDateRange.getEndYmd();
	}

	public String getStartYmd() {
		return mStartYmd;
	}

	public void setStartYmd(String startYmd) {
		mStartYmd = startYmd;
	}

	public String getEndYmd() {
		return mEndYmd;
	}

	public void setEndYmd(String endYmd) {
		mEndYmd = endYmd;
	}

	@Override
	public String toString() {
		return mStartYmd + "|" + mEndYmd;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NPDateRange that = (NPDateRange) o;

		if (mEndYmd != null ? !mEndYmd.equals(that.mEndYmd) : that.mEndYmd != null) return false;
		if (mStartYmd != null ? !mStartYmd.equals(that.mStartYmd) : that.mStartYmd != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = mStartYmd != null ? mStartYmd.hashCode() : 0;
		result = 31 * result + (mEndYmd != null ? mEndYmd.hashCode() : 0);
		return result;
	}
}
