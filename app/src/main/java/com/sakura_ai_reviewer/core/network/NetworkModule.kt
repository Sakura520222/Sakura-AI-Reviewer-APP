package com.sakura_ai_reviewer.core.network

import com.sakura_ai_reviewer.BuildConfig
import com.sakura_ai_reviewer.feature.auth.data.AuthApiService
import com.sakura_ai_reviewer.feature.dashboard.data.DashboardApiService
import com.sakura_ai_reviewer.feature.issue.data.IssueApiService
import com.sakura_ai_reviewer.feature.log.data.LogApiService
import com.sakura_ai_reviewer.feature.queue.data.QueueApiService
import com.sakura_ai_reviewer.feature.repo.data.RepoApiService
import com.sakura_ai_reviewer.feature.review.data.ReviewApiService
import com.sakura_ai_reviewer.feature.settings.data.SettingsApiService
import com.sakura_ai_reviewer.feature.user.data.UserApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenExpiryInterceptor: TokenExpiryInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenExpiryInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): DashboardApiService =
        retrofit.create(DashboardApiService::class.java)

    @Provides
    @Singleton
    fun provideReviewApiService(retrofit: Retrofit): ReviewApiService =
        retrofit.create(ReviewApiService::class.java)

    @Provides
    @Singleton
    fun provideIssueApiService(retrofit: Retrofit): IssueApiService =
        retrofit.create(IssueApiService::class.java)

    @Provides
    @Singleton
    fun provideSettingsApiService(retrofit: Retrofit): SettingsApiService =
        retrofit.create(SettingsApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideRepoApiService(retrofit: Retrofit): RepoApiService =
        retrofit.create(RepoApiService::class.java)

    @Provides
    @Singleton
    fun provideQueueApiService(retrofit: Retrofit): QueueApiService =
        retrofit.create(QueueApiService::class.java)

    @Provides
    @Singleton
    fun provideLogApiService(retrofit: Retrofit): LogApiService =
        retrofit.create(LogApiService::class.java)
}
