(ns roxxi.utils.time_test
  (:use clojure.test
        roxxi.utils.time))

(deftest consistent-now-consistent? []
  (testing "If consistent now "
    (with-consistent-now
      (testing "is consistent"
        (is (= (consistent-now) (consistent-now))))
      (testing "doesn't change"
        (Thread/sleep 500) ;; wait for a half second
        (is (not (= (consistent-now) (System/currentTimeMillis)))))
      (testing "returns seconds consistently"
        (is (= (quot (consistent-now) 1000) (consistent-now-sec)))))))

(deftest consistent-now-must-be-captured []
  (testing "if we try to get a consistent now when it's not captured
we get a Runtime Exception"
    (is (thrown? RuntimeException (consistent-now)))))

(deftest with-consistent-now-with-consistent-now-recapture-test []
  (testing "If I call with-consistent now multiple times, it shouldn't
recapture the time, but instead always just use the one that's already
captured"
    (with-consistent-now
      (let [t1 (consistent-now)]
        (Thread/sleep 200)
        (with-consistent-now
          (let [t2 (consistent-now)]
            (is (= t1 t2))))))))
