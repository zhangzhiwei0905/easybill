export default function Home() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
            <div className="max-w-4xl w-full bg-white rounded-2xl shadow-2xl p-8">
                <div className="text-center">
                    <h1 className="text-5xl font-bold text-gray-900 mb-4">
                        💰 EasyBill
                    </h1>
                    <p className="text-xl text-gray-600 mb-8">
                        智能账单管理系统
                    </p>

                    <div className="grid md:grid-cols-3 gap-6 mt-12">
                        <div className="bg-blue-50 rounded-xl p-6">
                            <div className="text-4xl mb-3">📱</div>
                            <h3 className="font-semibold text-lg mb-2">自动采集</h3>
                            <p className="text-sm text-gray-600">
                                iOS 快捷指令自动监听短信
                            </p>
                        </div>

                        <div className="bg-purple-50 rounded-xl p-6">
                            <div className="text-4xl mb-3">🤖</div>
                            <h3 className="font-semibold text-lg mb-2">AI 解析</h3>
                            <p className="text-sm text-gray-600">
                                DeepSeek 智能识别账单信息
                            </p>
                        </div>

                        <div className="bg-green-50 rounded-xl p-6">
                            <div className="text-4xl mb-3">📊</div>
                            <h3 className="font-semibold text-lg mb-2">数据可视化</h3>
                            <p className="text-sm text-gray-600">
                                多维度统计分析
                            </p>
                        </div>
                    </div>

                    <div className="mt-12 space-x-4">
                        <a
                            href="/transactions"
                            className="inline-block bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
                        >
                            查看账单
                        </a>
                        <a
                            href="/login"
                            className="inline-block bg-gray-200 text-gray-800 px-8 py-3 rounded-lg font-semibold hover:bg-gray-300 transition"
                        >
                            登录
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
}
