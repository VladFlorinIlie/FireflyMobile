package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.*
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import xyz.hisname.fireflyiii.workers.transaction.TransactionWorker
import java.math.BigDecimal
import kotlin.math.absoluteValue

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository
    val transactionAmount: MutableLiveData<String> = MutableLiveData()
    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }

    init {
        val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
        repository = TransactionRepository(transactionDataDao, transactionService)
    }

    fun getTransactionList(startDate: String?, endDate: String?, transactionType: String, pageNumber: Int): LiveData<List<Transactions>> {
        isLoading.value = true
        val data: MutableLiveData<List<Transactions>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
         //   data.postValue(repository.transactionList(startDate, endDate, transactionType, pageNumber))
        }.invokeOnCompletion {
            isLoading.postValue(false)
        }
        return data
    }

    fun getRecentTransaction(limit: Int): LiveData<MutableList<Transactions>>{
        isLoading.value = true
        var recentData: MutableList<Transactions> = arrayListOf()
        val data: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            recentData = repository.recentTransactions(limit)
        }.invokeOnCompletion {
            data.postValue(recentData)
        }
        return data
    }

    fun getWithdrawalAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<BigDecimal>{
        val data: MutableLiveData<BigDecimal> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val withdrawData = repository.allWithdrawalWithCurrencyCode(startDate, endDate, currencyCode)
            data.postValue(withdrawData.abs())
        }
        return data
    }

    fun getDepositAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<BigDecimal>{
        val data: MutableLiveData<BigDecimal> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val depositData = repository.allDepositWithCurrencyCode(startDate, endDate, currencyCode)
            data.postValue(depositData.abs())
        }
        return data
    }

    // My god.... the name of this function is sooooooo looong...
    fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                               currencyCode: String,
                                                               accountName: String): LiveData<Double>{
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        // TODO: Fix me, seriously
        viewModelScope.launch(Dispatchers.IO + exceptionCoroutine){
            transactionAmount = repository.getTransactionsByAccountAndCurrencyCodeAndDate(startDate, endDate, currencyCode, accountName)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    private val exceptionCoroutine = CoroutineExceptionHandler{ a,b -> }

    fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueCategoryByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
        }
        return data
    }

    fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                              sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueBudgetByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                             currencyCode: String, accountName: String,
                                                   transactionType: String): MutableLiveData<Double>{
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, accountName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String,
                                                   transactionType: String): MutableLiveData<Double>{
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTotalTransactionAmountAndFreqByDateAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String,
                                                          transactionType: String,
                                                          currencySymbol: String): MutableLiveData<TransactionAmountMonth>{
        var transactionAmount = 0.0
        var transactionFreq = 0
        val transactionData: MutableLiveData<TransactionAmountMonth> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, transactionType)
            transactionFreq = repository.transactionList(startDate, endDate, transactionType).size
        }.invokeOnCompletion {
            transactionData.postValue(TransactionAmountMonth(DateTimeUtil.getMonthAndYear(startDate),
                    currencySymbol + LocaleNumberParser.parseDecimal(transactionAmount, getApplication()),
                    transactionFreq))
        }
        return transactionData
    }

    fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String, accountName: String,
                                                   transactionType: String, categoryName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndCategoryAndCurrency(startDate, endDate,
                    currencyCode, accountName, transactionType, categoryName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                  currencyCode: String, accountName: String,
                                                  transactionType: String, budgetName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndBudgetAndCurrencyAndAccountName(startDate, endDate,
                    currencyCode, accountName, transactionType, budgetName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                            accountName: String): MutableLiveData<MutableList<Transactions>>{
        val transactionData: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        var data: MutableList<Transactions> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
            data = repository.getTransactionListByDateAndAccount(startDate, endDate, accountName)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun addTransaction(type: String, description: String,
                       date: String, piggyBankName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?,
                       fileUri: ArrayList<Uri>, notes: String): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        transactionService?.addTransaction(convertString(type),description, date ,piggyBankName,
                amount.replace(',', '.'),sourceName,destinationName,currencyName,
                category, tags, budgetName, notes)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBodyMessage)
                try {
                    moshi?.errors?.transactions_currency?.let {
                        errorBodyMessage = moshi.errors.transactions_currency[0]
                    }
                    moshi?.errors?.piggy_bank_name?.let {
                        errorBodyMessage = moshi.errors.piggy_bank_name[0]
                    }
                    moshi?.errors?.transactions_destination_name?.let {
                        errorBodyMessage = moshi.errors.transactions_destination_name[0]
                    }
                    moshi?.errors?.transactions_source_name?.let {
                        errorBodyMessage = moshi.errors.transactions_source_name[0]
                    }
                    moshi?.errors?.transaction_destination_id?.let {
                        errorBodyMessage = moshi.errors.transaction_destination_id[0]
                    }
                    moshi?.errors?.transaction_amount?.let {
                        errorBodyMessage = "Amount field is required"
                    }
                    moshi?.errors?.description?.let {
                        errorBodyMessage = moshi.errors.description[0]
                    }
                } catch (exception: Exception){
                    errorBodyMessage = "The given data was invalid"
                }
            }
            if (response.isSuccessful) {
                var transactionJournalId = 0L
                viewModelScope.launch(Dispatchers.IO){
                    response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                        transactionJournalId = transaction.transaction_journal_id
                        repository.insertTransaction(transaction)
                        repository.insertTransaction(TransactionIndex(response.body()?.data?.transactionId,
                                transaction.transaction_journal_id))
                    }
                }.invokeOnCompletion {
                    if(fileUri.isNotEmpty()){
                        AttachmentWorker.initWorker(fileUri, transactionJournalId, getApplication())
                    }
                }
                transaction.postValue(ApiResponses(response.body()))
            } else {
                transaction.postValue(ApiResponses(errorBodyMessage))
            }
        })
        { throwable -> transaction.value = ApiResponses(throwable) })
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    fun updateTransaction(transactionJournalId: Long, type: String, description: String,
                          date: String, amount: String,
                          sourceName: String?, destinationName: String?, currencyName: String,
                          category: String?, tags: String?, budgetName: String?,
                          fileUri: ArrayList<Uri>, notes: String): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO){
            transactionId = repository.getTransactionIdFromJournalId(transactionJournalId)
        }.invokeOnCompletion {
            transactionService?.updateTransaction(transactionId, convertString(type), description, date,
                    amount.replace(',', '.'), sourceName, destinationName, currencyName,
                    category, tags, budgetName, notes)?.enqueue(retrofitCallback({ response ->
                val errorBody = response.errorBody()
                var errorBodyMessage = ""
                if (errorBody != null) {
                    errorBodyMessage = String(errorBody.bytes())
                    val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBodyMessage)
                    try {
                        moshi?.errors?.transactions_currency?.let {
                            errorBodyMessage = moshi.errors.transactions_currency[0]
                        }
                        moshi?.errors?.piggy_bank_name?.let {
                            errorBodyMessage = moshi.errors.piggy_bank_name[0]
                        }
                        moshi?.errors?.transactions_destination_name?.let {
                            errorBodyMessage = moshi.errors.transactions_destination_name[0]
                        }
                        moshi?.errors?.transactions_source_name?.let {
                            errorBodyMessage = moshi.errors.transactions_source_name[0]
                        }
                        moshi?.errors?.transaction_destination_id?.let {
                            errorBodyMessage = moshi.errors.transaction_destination_id[0]
                        }
                        moshi?.errors?.transaction_amount?.let {
                            errorBodyMessage = "Amount field is required"
                        }
                        moshi?.errors?.description?.let {
                            errorBodyMessage = moshi.errors.description[0]
                        }
                    } catch (exception: Exception){
                        errorBodyMessage = "The given data was invalid"
                    }
                }
                if (response.isSuccessful) {
                    viewModelScope.launch(Dispatchers.IO) {
                        viewModelScope.launch(Dispatchers.IO){
                            response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                                repository.insertTransaction(transaction)
                                repository.insertTransaction(TransactionIndex(response.body()?.data?.transactionId,
                                        transaction.transaction_journal_id))
                            }
                        }
                    }.invokeOnCompletion {
                        if(fileUri.isNotEmpty()){
                            AttachmentWorker.initWorker(fileUri, transactionJournalId, getApplication())
                        }
                    }
                    transaction.postValue(ApiResponses(response.body()))
                } else {
                    transaction.postValue(ApiResponses(errorBodyMessage))
                }
            })
            { throwable -> transaction.value = ApiResponses(throwable) })
        }
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    fun getTransactionByJournalId(transactionJournalId: Long): LiveData<MutableList<Transactions>>{
        val transactionData: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        var data: MutableList<Transactions> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO){
            data = repository.getTransactionByJournalId(transactionJournalId)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun deleteTransaction(transactionJournalId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        var isItDeleted = 0
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            transactionId = repository.getTransactionIdFromJournalId(transactionJournalId)
            if(transactionId == 0L){
                // User is offline and transaction is pending. Cancel work
                TransactionWorker.cancelWorker(transactionJournalId, getApplication())
            }
            isItDeleted = repository.deleteTransactionById(transactionId)
        }.invokeOnCompletion {
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteTransactionWorker.setupWorker(transactionId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
            isLoading.postValue(false)
        }
        return isDeleted
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    fun getTransactionAttachment(journalId: Long): MutableLiveData<MutableList<AttachmentData>>{
        isLoading.value = true
        val attachmentRepository = AttachmentRepository(AppDatabase.getInstance(getApplication()).attachmentDataDao())
        val data: MutableLiveData<MutableList<AttachmentData>> = MutableLiveData()
        var attachmentData: MutableList<AttachmentData> = arrayListOf()
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            transactionId = repository.getTransactionIdFromJournalId(journalId)
        }.invokeOnCompletion {
            transactionService?.getTransactionAttachment(transactionId)?.enqueue(retrofitCallback({ response ->
                if (response.isSuccessful) {
                    response.body()?.data?.forEachIndexed { _, attachmentData ->
                        viewModelScope.launch(Dispatchers.IO) {
                            attachmentRepository.insertAttachmentInfo(attachmentData)
                        }
                    }
                    data.postValue(response.body()?.data)
                    isLoading.value = false
                } else {
                    /** 7 March 2019
                     * In an ideal world, we should be using foreign keys and relationship to
                     * retrieve related attachments by transaction ID. but alas! the world we live in
                     * isn't ideal, therefore we have to develop a hack.
                     *
                     * P.S. This was a bad database design mistake I made when I wrote this software. On
                     * hindsight I should have looked at James Cole's design schema. But hindsight 10/10
                     **/
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                            isLoading.postValue(false)
                            data.postValue(attachmentData)
                        } catch (exception: Exception){ }
                    }
                }
            })
            { throwable ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                        isLoading.postValue(false)
                        data.postValue(attachmentData)
                    } catch (exception: Exception){ }

                }
                apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
            })
        }
        return data
    }

    fun getTransactionByDescription(query: String) : LiveData<List<String>>{
        val transactionData: MutableLiveData<List<String>> = MutableLiveData()
        val displayName = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactionByDescription(query)
                    .collectLatest { transactionList ->
                        transactionList.forEach { transactions ->
                            displayName.add(transactions.description)
                        }
                        transactionData.postValue(displayName.distinct())
            }
        }
        return transactionData
    }

}