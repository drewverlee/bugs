;; TODO picture bug

;; I was filling out a job application and one of the questions was to

;; > Describe a bug you had a primary role in fixing. How did you troubleshoot and resolve the issue?

;; Well, I thought, do you want the long or short answer? The short? Fine then, the bug was a
;; mistranslation, and to troubleshoot, I tested, typed until I *triumphed!*

;; Too short? Maybe, but I fear the full story would be too long.

;; And so we have a choice. Do we tell the long grueling fullness that would be the truth? Or a
;; flattering short lie? Do I try to cast you into the pits of confusion and misguide you such that you
;; stumble as i did? Or do i build a bridge of perfect clarity and pretend I
;; too solved it by walking a straight line?

;; Most importantly... has the interviewer already stopped reading this and
;; moved on? Maybe, but I
;; have to hope they care, as I do, about the journey and not the destination.
;; And journeys aren't straight line, there winding rivers that flows back into themselves. The landscape around us
;; changes little, and in the end, it's our precipitations and not the world the bends.

;; So then, let me set you along the riverbank at a place which could be called
;; a beginning.

;; TODO riverbank picture.

;; From there you will travel onward until the end, and your task  will be to keep
;; your eyes open and see if you can catch the bug before it bites.

;; Imagine your with your friends on a trip, you stop and have
;; lunch, the bill comes and the waiter didn't split it. Not wanting to spoil
;; the moment with the technicalities of math, you offer graciously to pay for
;; everyone. Tomorrow, at dinner, someone else covers the part. The
;; trend continues, it becomes a game of sorts. At the end of the trip however, on the plane
;; back, everyone becomes suddenly concerned they didn't pay enough, but their
;; not sure who owes who at this point.

;; TODO picture of friends having dinner

;; This very thing happened to me, and I was charged with telling people who owes who and how much.

;; I'll spare you the details of calling people to ask them to clarify what they
;; paid for, or having to look up who went to which dinner. Let's instead,
;; assume your magically given a clean simple ledger that looks something like
;; this:

;; * katie buys 10$ ice-cream for drew, and katie.
;; * kirsten buys katie and drew $50 tickets each to ride up to the top of the offal tower.
;; * drew buys katie a 5 dollar water

;; so we have our input, lets keep our eye on the destination, we need to end up
;; with a set of debts to be repaid. I'm using the word set in the sense that
;; the order doesn't matter, and we shouldn't have duplicates. Why? Because in
;; addition to figuring out who owes who, we want to do it in a few steps as
;; possible to save people's time and money (if there are transfer fees).

;; so we want to avoid this: drew pays kirsten 10, then kirsten 5. Instead, drew should just pay kirsten 5.

;; so lets write down any axioms we can think of
;; * at the end, no one should have any outstanding loans
;; * the number of loans at the end, shouldn't exceed the number of loaners
;; * the minimal number of loans at the end can't be less then 0

;; Ok, those principles should guide us to a solution and so something we can
;; test. Firstly we have to encode the data, that encoding, comes directly from
;; the problem. Were given a list of loans, which can have duplicates, and were
;; to return a list of loans, that can't have duplicates, and where we have ... and this is where Englisher fails...
;; consolidated loans.

;; If your having a hard time find the right words to describe your problem, it's about a million times more likely that you
;; simply don't know how people describe this issue, then you have discovered a brand new problem or way of thinking.

;; in this case, the key insight is to realize that the shape of our input, and output, are both graphs.

;; A -- 5 --> B
;;

#_(def mermaid-viewer
  {:transform-fn clerk/mark-presented
   :render-fn    '(fn [value]
                 (when value
                   [nextjournal.clerk.render/with-d3-require {:package ["mermaid@8.14/dist/mermaid.js"]}
                    (fn [mermaid]
                      [:div {:ref (fn [el] (when el
                                             (.render mermaid (str (gensym)) value #(set! (.-innerHTML el) %))))}])]))})

;; ### INPUT Graph

;; (clerk/with-viewer mermaid-viewer
;;   "graph
;;     A((Drew)) -- 10 --> B((Kirsten))
;;     B((Kirsten)) -- 5 --> A((Drew))
;;     A((Drew)) -- 5 --> C((Katie))")


;; ### OUTPUT graph

;; (clerk/with-viewer mermaid-viewer
;;   "graph
;;     A((Drew)) -- 5 --> B((Kirsten))
;;     A((Drew)) -- 5 --> C((Katie))")

;; At this point, we want to ask google or chatgpt if they have any ideas. Here
;; is my chatgpt prompt:

;; What graph algorithm would transform a directed Cycle graph with weighted
;; edges into a directed graph with no cycles. The cycles would be removed by
;; having the two edges in a cycle between two nodes subtracted from each other
;; to create a single edge.

;; Chatgpt doesn't have a name for this, but it did give some clojure code.
;; Spoiler, it didn't work. I asked google the same thing, no luck. It's just
;; too detailed a question, i'm guessing it will be slightly easier to write
;; the code ourselves.


;; The first thing to notice is that, because we want to minimize transactions, we will want to consolidate how much someone is owed, and how much they owe. After all, we don't care who pays
;; back who at the end, just that everyone gets paid back. So we want to go from this:

["drew" "kirsten" 10]
["drew" "katie" 5]

;; ;; to this:

{"drew" 15
 "katie" -5
 "kirsten" -10}


;; the choice of using pos to represent that drew is owed 10 is arbitrary, but we have to keep it straight, positive means your owed money!
;; go ahead and write the code now to do this. I'll wait. Ok time to show your work:

(->> [["drew" "kirsten" 10]
      ["drew" "katie" 5]]
     (reduce
       (fn [n->v [s e v]]
         (-> n->v
             (update s (fnil + 0) v)
             (update e (fnil - 0) v)))
       {}))


;; great!

;; ok, now for the hard part. Describing what we want to do not in terms of our
;; problem, but in terms a way we can write in code. I would say, given a list
;; of id (e.g drew) values (e.g 15) tuples, we want to match them together two
;; at a time, and for each match produce two things:

;; 1. the reminder of adding the Values together which will be attached to id2
;; and put back on our list-of-id-values

;; 2. the result of adding the Values
;; together, which will be attached to a triplet with both ids and serve to tell
;; us that the transaction (the addition) happened between those ids.

;; one output triplet that encodes that id1 id2 (+ v1 v2) where (+ v1 v2)
;; depending on if it's positive or negative will tell us if id1 gave id2 (+ v1
;; v2) or vise versa. Becau

;; the idea is that we just have to create a set of loans,
;; really a list of x + y, where x is the amount someone is owed or lent + the
;; amount someone else is owed or lent. Such that at the end we get zero. so look

;; 15 - 10 = 5
;; 5 - 5 = 0
;; done
;; that translates to
;; drew kirsten 10
;; drew katie 5

;; which is exactly what we wanted. So we can see how were going to recursively
;; do work until we hit zero.

(->>{"drew" 15, "katie" -5, "kirsten" -10}
    (into [])
    (sort-by (comp abs second))
    vec)

(loop [i [["katie" -5] ["kirsten" -10] ["drew" 15]]
       o []]
  (if (>= 1 (count i)) (set o)
    (let [[[id1 id1v] [id2 id2v]] [(-> i pop peek) (peek i)]]
      (recur
        (conj (-> i pop pop) [id2 (+ id2v id1v)])
        (conj o [id1 id2 id1v])))))


;; as we already discussed, the way to read this is kirsten gives drew -10
;; dollars, which should be interpreted as drew gives kirsten 10. at this point,
;; i'm sure people are tempted to argue that i should give things better names,
;; and we can *now*, but its critical to realize that the ambiguity has to be
;; settled after the creation of this triplet, and can't be done before, so this
;; giving things hanes before would be confusing. This triplet is just that, a
;; relationship of three things and each thing impacts the other, so individual
;; names can only be derived in context of the whole.

;; now we can get the loans we desired.
(->> #{["katie" "drew" -5] ["kirsten" "drew" -10]}
     (map (fn [[u1 u2 v :as t]] (if (pos? v) t [u2 u1 (abs v)])))
     (map (fn [[u1 u2 v]] {:loaner u1 :loanee u2 :loan v})))

;; or the debts, depending on how the data is used.

(->>  #{["katie" "drew" -5] ["kirsten" "drew" -10]}
      (map (fn [[u1 u2 v :as t]] (if (pos? v) t [u2 u1 (abs v)])))
      (map (fn [[u1 u2 v]] {:debtee u1 :debtor u2 :debt v})))

;; returning to the body of our logic though, we should defiantly write some tests.
;; that means naming our function, its tempting here to go with something related to loans, or users, but really
;; the logic here is about going from nodes to edges

;;


(defn nodes-to-min-value-edges-graph
  [nodes]
  (loop [nodes nodes
         edges []]
    (if (>= 1 (count edges)) (set edges)
        (let [[[id1 id1v] [id2 id2v]] [(-> nodes pop peek) (peek nodes)]]
          (recur
            (conj (-> nodes pop pop) [id2 (+ id2v id1v)])
            (conj edges [id1 id2 id1v]))))))
