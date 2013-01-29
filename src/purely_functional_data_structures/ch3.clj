(ns purely-functional-data-structures.ch3
  (:refer-clojure :exclude [merge]))

;;
;; Chapter 3 - Familiar Data Structures in a Functional Setting
;;


;;
;; Leftist heaps
;;

;;
;; Using protocols
;;


(defprotocol Heap
  (is-empty?   [this])
  (insert      [this v])
  (merge       [this other])
  (rank        [this])
  (find-min    [this])
  (delete-min  [this]))

(defrecord LeftistHeap [rank value left right])

(defn ensure-leftist
 [this other v]
 (let [rank-this (rank this)
       rank-other (rank other)]
   (if (>= rank-this rank-other)
     (->LeftistHeap (inc rank-other) v this other)
     (->LeftistHeap (inc rank-this) v other this))))

(extend-protocol Heap
  nil
  (rank [_] 0)
  (merge [_ other] other)
  (is-empty? [_] true)

  LeftistHeap
  (is-empty? [this]
    (nil? this))

  (rank [this]
    (:rank this))
  
  (merge [{val-this :value left-this :left right-this :right :as this}
          {val-other :value left-other :left right-other :right :as other}]
    (cond
     (is-empty? other) this
     :else (if (<= val-this val-other)
             (ensure-leftist left-this
                             (merge right-this other)
                             val-this)
             (ensure-leftist left-other
                             (merge this right-other)
                             val-other))))
  
  (insert [this v]
    (merge (->LeftistHeap 1 v nil nil)
           this))

  (find-min [{v :value}] v)
  
  (delete-min [{left :left right :right}]
    (merge right left)))


;;
;; Using pure functions and maps
;;


(defn mk-heap [rank value left right]
  {:rank rank :value value :left left :right right})

(defn heap-rank [heap]
  (if (nil? heap)
    0
    (:rank heap)))

(defn ensure-leftist-heap [value heap-a heap-b]
  (let [rank-a (heap-rank heap-a)
        rank-b (heap-rank heap-b)]
    (if (>= rank-a rank-b)
      (mk-heap (inc rank-b) value heap-a heap-b)
      (mk-heap (inc rank-a) value heap-b heap-a))))

(defn merge-heaps [{val-a :value left-a :left right-a :right :as heap-a}
                   {val-b :value left-b :left right-b :right :as heap-b}]
  (cond
   (nil? heap-a) heap-b
   (nil? heap-b) heap-a
   :else (if (<= val-a val-b)
           (ensure-leftist-heap val-a
                                left-a
                                (merge-heaps right-a heap-b))
           (ensure-leftist-heap val-b
                                left-b
                                (merge-heaps heap-a right-b)))))

(defn heap-insert [value heap]
  (merge-heaps (mk-heap 1 value nil nil)
               heap))

(defn heap-find-min [{v :value}] v)
  
(defn heap-delete-min [{left :left right :right}]
  (merge-heaps right left))


;;
;; Exercises - p19
;;

;;
;; 3.2 
;;

(defn direct-heap-insert
  [value {val-b :value left-b :left right-b :right :as heap-b}]
  (cond (nil? heap-b) (mk-heap 1 value nil nil)
        (< value val-b) (ensure-leftist-heap value heap-b nil)
        :else (ensure-leftist-heap val-b left-b
                                   (direct-heap-insert value right-b))))

;;
;; 3.3
;;

(defn heap-from-list-O-n
  [coll]
  (reduce (fn [acc i]
            (merge-heaps (if (map? acc) acc (mk-heap 1 acc nil nil))
                         (mk-heap 1 i nil nil))) coll))


(defn mk-singleton-heap [n]
  (mk-heap 1 n nil nil))

(defn heap-from-list-O-log-n
  [coll]
  (let [singleton-heaps (map mk-singleton-heap coll)]
    (loop [heaps singleton-heaps]
      (if (= (count heaps) 1)
        (first heaps)
        (recur (map (fn [pair] (apply merge-heaps pair))
                    (partition 2 2 [nil] heaps)))))))

