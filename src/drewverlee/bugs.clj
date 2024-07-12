;; # The Seven Guiding Principles for Troubleshooting Software Bugs

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html
 [:img {:src "https://docs.google.com/drawings/d/e/2PACX-1vQ6rsadfjdL3n7KjCPLGknAhV5x8jen8M0xvyVWtNGrAPat80_BFyvkl7xLjyReY383gXGOCATZ9G56/pub?w=960&amp;h=720"}])

;; How do you fix bugs in your software? Is there a process, and if so, can
;; it be taught, or is it a path can only be walked?

;; These questions came to me on the heels of another that I was asked recently on a
;; job application:

;; > Describe a bug you had a primary role in fixing. How did you troubleshoot and resolve the issue?

;; Well, I thought, do you want the long or short answer? The short you say? Fine then,
;; the bug was a mistranslation, and to troubleshoot, I tested, typed until I
;; *triumphed!*

;; Too short? Maybe, but I fear the full story would be too long.

;; Can we settle on somewhere in between a flattering short lie, and the confusing long truth.

;; Most importantly... has the interviewer already stopped reading this and
;; moved on? Maybe, but I have to hope they care, as I do, about the journey and
;; not the destination. And journeys aren't straight line, there winding rivers
;; that flows back into themselves. The landscape around us changes very little, and
;; in the end, it's our precipitations and not the world the bends.

;; So then, let me set you along the riverbank at a place which could be called
;; a beginning.

;; From there you will travel onward until the end, and your task will be to
;; keep your eyes open and see if you can catch the bug before it bites.

;; Let me motivate the problem were trying to solve, after all, if you don't understand
;; the solution, how can you hope to understand the nature of the problem?

;; TODO picture of friends having dinner



;; So then, imagine your with your friends on a trip, you stop and have lunch,
;; the bill comes and the waiter didn't split it. Not wanting to spoil the
;; moment with the technicalities, you offer graciously to pay for everyone.
;; Tomorrow, at dinner, someone else covers the part. The trend of having someone pay
;; for the group continues.
;;
;; Howevever at the end of the trip, everyone becomes suddenly concerned they
;; didn't pay enough, but their not sure who owes who what.

;;  * katie buys 10$ ice-cream for drew, and katie. kirsten buys katie and drew
;;  * $50 tickets each to ride up to the top of the offal tower. ;; ;; * drew
;;  * buys katie a 5 dollar water

;; We need to turn this into a set of loans to be repaid. Oh, and wouldn't it be
;; a nice, because time is money, and sometimes there are transfering fees, to
;; guartnee it's the minimal number of loans needed? Avoiding silly cycles like:
;; drew pays kirsten 10, then kirsten 5. And other such situations.

;; TODO picture of dragons

;; At this point, it would be often be useful to start asking chatGPT or
;; searching Google for similar problems. And I did. However, down that path
;; lays dragons. The issue is that explaining WHY that will lead you astray is
;; somewhat harder then working through the example and avoiding. Maybe will do
;; that at the end, for now, can you trust me that it's easier to stay isolated?

;; Thanks i apperciate it.

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(require '[nextjournal.clerk :as clerk])

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def mermaid-viewer
  {:transform-fn clerk/mark-presented
   :render-fn    '(fn [value]
                    (when value
                      [nextjournal.clerk.render/with-d3-require {:package ["mermaid@8.14/dist/mermaid.js"]}
                       (fn [mermaid]
                         [:div {:ref (fn [el] (when el
                                                (.render mermaid (str (gensym)) value #(set! (.-innerHTML el) %))))}])]))})

;; Instead of searching, lets see if visualizing the problem helps, and a useful
;; visualization, like a useful description, will try to remove ambiguity. In
;; this case we represent a loan by an arrow/edge where the direction indicates
;; which way the money travels. Here is how we would translate our problem above:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/with-viewer mermaid-viewer
  "graph
    A((Drew)) -- 10 --> B((Kirsten))
    B((Kirsten)) -- 5 --> A((Drew))
    A((Drew)) -- 5 --> C((Katie))")


;; And here is the graph after we consolidate the loans:


^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/with-viewer mermaid-viewer
  "graph
    A((Drew)) -- 5 --> B((Kirsten))
    A((Drew)) -- 5 --> C((Katie))")

;; This is just one example, but it already might start to tell us things about the nature of the problem.
;; What do you see?

;; TODO picture of something

;; Well, if you don't see anything, then keep adding cases until you do. If
;; by around the 7th or so, still nothing jumps out to you. Consider a new way
;; to vizualize the issue, or consider your not correctly capturing your or end
;; state.

;; Regardless, and this is your another trouble shooting tip, find a pattern.
;; Here is the pattern that jumps out to me, there is only one


;; and how much they owe
;; into what is commonly refered to as net. net or net-worth, which will define
;; here as owed - owe

;; Here is the code to do that:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn loans->net-worth
  [loans]
  (->> loans
       (reduce
        (fn [n->v {:keys [loaner loanee loan]}]
          (-> n->v
              (update loaner (fnil + 0) loan)
              (update loanee (fnil - 0) loan)))
        {})
       (reduce-kv
        (fn [s id net]
          (conj s {:user/id id :user/net-worth net}))
        #{})))

(loans->net-worth [{:loaner "drew" :loanee "kirsten" :loan 10}
                   {:loaner "drew" :loanee "katie" :loan 5}])


;; My first thought was that each step the function would need to take two
;; users, create a loan between them, and if there was a remaining net-worth,
;; return it and it's owner.

;; Beause we want the minimal amount of transactions, will want to have the minimal
;; amount of distance between the nets, ensuring the highest likely hood they will
;; cancle out.

;; Here is an implementation which does just that:


^{:nextjournal.clerk/visibility {:result :hide}}
(defn net-worth->consolidated-loans
  [net-worth]
  (loop [net-worth (vec (sort-by (comp abs :user/net-worth) net-worth))
         loans []]
    (if (<= (count net-worth) 1)
      (->> loans
           (map (fn [[id1 id2 loan]]
                  (if (pos? loan)
                    {:loaner id1 :loanee id2 :loan loan}
                    {:loaner id2 :loanee id1 :loan (abs loan)})))
           set)
      (let [[{nw1 :user/net-worth id1 :user/id}
             {nw2 :user/net-worth id2 :user/id}] [(-> net-worth pop peek) (peek net-worth)]]
        (recur
         (conj (-> net-worth pop pop) {:user/id id2 :user/net-worth (+ nw2 nw1)})
         (conj loans  [id1 id2 nw1]))))))


(net-worth->consolidated-loans
 #{{:user/id "drew", :user/net-worth 15}
   {:user/id "katie", :user/net-worth -5}
   {:user/id "kirsten", :user/net-worth -10}})

;; I added several more tests and they all matched my expectation. However, the next day when I revisisted
;; the problem and reviewed it with fresh eyes. Two things stood out to me.

;; First, sorting by absolute value might result in placing two net-values of the same sign (+, -) next to each
;; other, and that would actual produce an extra transaction, though it would still correctly balance out.

;; This would seem to be easily fixed by using a posative and negative list.

;; The second issue I saw was a bit more subtle, this is the real bug. I'm curious if you see the issue?

;; Think about what it means to minimize transactions. Maybe take a walk and
;; roll the idea around in your head. That's what I did, and It's a key part of
;; how I troubleshoot issues.

;; Before we jump into troubleshooting this the rest of the way, I want to take
;; a moment and dicuss how I think it's important to frame this issue with our
;; solution a slightly different way.

;; ## Bugs

;; In the software community a 'bug' commonly refers to any issue with the
;; software. To a entomogists, a bug seems to be anything with a piercing mouth
;; that sucks juices from plants or animals. And to everyone else, it's those
;; very little things that fly or crawl around.

;; And I strongly feel that under the generic meaning we attach to it the software community of describing
;; any 'problem' we run across, there lays the idea that the problem is something, like a misquto, that needs
;; to be removed.

;; However, I find it far more common instead of having to remove some intruder from my program, instead,
;; what i need to do to realize my desires is understand what those really are in the first place.

;; Put another way, most 'issues' I run across feel, in retrospect, like I was
;; upset that sunflowers didn't taste good despite my constant care, and
;; attention to watering them.

;; Put another way, I didn't need to remove something from my sunflower, it's simply that what I really
;; wanted to grow was tomatoes. I was growing the wrong, thing, so it was a weed.

;; TODO picture of a weed.

;; ## Weed or Bug?

;; I want make a suggestion to you, my reader, of not saying "we have a bug", but asking
;; Is this a bug or a weed?

;; While this distintion seems fuzzy in that both bugs and weeds are both
;; undesirable things, a weeds defining characterstic is only that it's simply not the plant you wanted.

;; So, the issue isn't the weed you have, it's the plant you don't. A weed
;; incidates something vital is lacking. A bug suggets something needs to be
;; removed.

;; What matters is that the question starts to divide the problem.

;; And breaking the problem apart is the heart of effective troubleshooting. This is how I troubleshoot problems.

;; ## How does your garden grow?

;; How does our algorithm grow such at each step we ensure the minimal number of transactions?
;; First off, we need to strip away the ambiguity of this question.


;; A transaction is anytime we add two sets of numbers and get a non-zero
;; result.

;; So to avoid transactions, we want results that equal zero. Put another way,
;; we want to break our set, really a multiset (duplicates allowed), into as many subsets
;; as possible where each subset sums to 0, and themselves contain no subset which sums to 0.

;; The easiest way to capture the equation needed to solve this to start with the assumption we
;; have a list of every possible permutation of the integers, then check how many subsets we can create
;; from each permutation, and then choose the the result with the most.

;; So what were asking is really how can we take a step, such that we
;; make progress towards the result that has the maxium number of sets that
;; equal zero, where each set itself contains no subset which equals zero.

;; So at this point, I believe our only course of action, to be 100% we find the minimal
;; number of transactions. Is to try every possible permutation.

;; looking at this example, we can can see how once we have an individual permutation


;; => ((1 -1 -2 2) ;; 2 subsets because 1 + -1 = 2 and -2 + 2 = 0
;;     ...
;;     (1 -2 -1 2) ;; 1 subset because 1 + -2 = -1 and then -1 + -1 = -2 and finally -2 + 2 = 0
;;    ....

;; However the run time complexity of finding all permutations that is n! or (n)(n-1)..(n-n).

;; So is there a better way? Or maybe cacheing can help?
;; I wasn't able to quickly see any better solutions, at first I thought if i could find
;; a way to select 2 numbers, such that my next selection would sum to 0, that would help,
;; but i quickly realized thats just kicking the can, as a 0 sum might not be possible in the next
;; solution either.

;; a bit disheartned, because I knew a solution with permutations wouldn't scale very well,
;; I quickly coded it to at least soldiify the idea

(require '[clojure.math.combinatorics :refer [permutations]])

(permutations [1 -1 -2 2])

(defn integers->zero-sum-integers
  [integers]
  (:zero-sum-lists
   (reduce
     (fn [{:keys [sum zero-sum-list] :as m} integer]
      (let [new-sum (+ sum integer)
            new-list (conj zero-sum-list integer)]
        (->
         (if (zero? new-sum)
           (-> m
               (update :zero-sum-lists conj new-list)
               (assoc :zero-sum-list []))
           (assoc m :zero-sum-list new-list))
         (assoc :sum new-sum))))
    {:sum 0 :zero-sum-lists [] :zero-sum-list []}
    integers)))

(->>
 (permutations [5 4 -3 -3 -2 -1])
 (pmap integers->zero-sum-integers)
 (sort-by count)
 last
 (map #(sort > %)))

;; ##  Looking afield


;; Here at the end I promised i would explain why its better to avoid searching
;; for the answer to early before your very sure what your looking for. As an
;; example inital search results for this problem bring up this [SO](
;; https://stackoverflow.com/questions/877728/what-algorithm-to-use-to-determine-minimum-number-of-actions-required-to-get-the)


;; And as hopefullly you know see, none of those answers do what we wanted. (Well, maybe mbirons)

;; The [second link](https://softwareengineering.stackexchange.com/questions/337125/finding-the-minimum-transaction)
;; and [third](https://softwareengineering.stackexchange.com/questions/337125/finding-the-minimum-transaction) link also, as afar as i can tell, fail to offer 100% correct solutions.

;; Maybe our AI overloards can help? Here is what I asked chatgpt:

;; > What is the name of the algorithm that would ensure the minimal number of
;; >  transactions between a set of givers and receivers whose total sums to 0? E.g
;; >  a give has 1 to give, and a receiver has 1 to receive. So the minimal number
;; >  of transactions would be 1.

;; ChatGPT responded with...

;; > Here is the important part of it's response:
;; > The algorithm you're referring to is known as the "minimum cost flow" algorithm in the context of network flow problems...

;; The [minimum cost flow algorithm](https://en.wikipedia.org/wiki/Minimum-cost_flow_problem) i'm relative sure,
;; doesn't guartnee the minimal amount of transactions. This [post](https://medium.com/@subhrajeetpandey2001/splitwise-a-small-approach-of-greedy-algorithm-4039a1e919a6#:~:text=The%20Debt%20Simplification%20Algorithm%20used,as%20few%20edges%20as%20possible.) has this to say about the minimal cost flow algorithm:

;; > Here’s a simplified version of the algorithm:

;; > Calculate the net balance for each member in the group.
;; > While there are outstanding balances:
;; > Identify the member owed the most (vmax) and the member owing the most (vmin).
;; > Transfer the minimum of |vmax| and |vmin| from vmax to vmin.
;; > Update the net balances for both members. If the balance becomes 0, remove them from the set of vertices.
;; > 3. Repeat steps 2 until all balances are settled.

;; > This algorithm sacrifices the rule that “No one owes a person that they didn’t
;; >  owe before” but efficiently settles balances. While its time complexity is
;; >  O(V²) and space complexity is O(V), it may require more transactions than
;; >  necessary.

;; Two things to note there, the most important is that the author agrees with my
;; intution that it may require more transactions then necessary and if your read
;; the steps, it's basically what we tried on our second step after we realized we
;; needed two lists, so we KNOW it doesn't work.

;; And finally here is a [post](https://medium.com/@mithunmk93/algorithm-behind-splitwises-debt-simplification-feature-8ac485e97688) agreeing that the:

;; > This indicates that the debt simplification problem is at least as hard as the Sum of Subsets Problem and hence it is NP-Complete

;; However the author is making, i believe, a gross understatment. the subset
;; problem finds out if there exists a subset of numubers that equal a given
;; number, discovering the maxium number of sets that the orginal set can break
;; into that equal that number is might be a degree or two more work.


;; ### Troubling results

;; The goal here, beyond to figure out a way to minimize transactions, was to
;; understand if there was a process to trouble shooting and if it could be taught.
