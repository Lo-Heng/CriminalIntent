package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2017-10-11.
 */

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks,CrimeFragment.Callbacks {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePageActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
    @Override
    public void onCrimeDeleted(Crime crime) {
        // 如果只是删除了一个，而还有其他的 Crime 的话，
        // 就相当于选中一个 Crime，这里传过来的应该是第一个 Crime


        onCrimeSelected(crime);
    }

    @Override
    public void onCrimeAllDeleted(Crime crime) {
        // 如果全部删除，就直接将该 fragment 移去
        CrimeFragment fragment = (CrimeFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.detail_fragment_container);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        // 并且更新列表页
        onCrimeUpdated(crime);
        if (findViewById(R.id.detail_fragment_container) == null) {
            this.finish();
        }

    }
}
